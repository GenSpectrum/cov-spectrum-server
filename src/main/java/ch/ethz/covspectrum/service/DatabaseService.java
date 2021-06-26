package ch.ethz.covspectrum.service;

import ch.ethz.covspectrum.entity.api.*;
import ch.ethz.covspectrum.entity.core.DataType;
import ch.ethz.covspectrum.entity.core.*;
import ch.ethz.covspectrum.jooq.MyDSL;
import ch.ethz.covspectrum.jooq.SpectrumMetadataTable;
import ch.ethz.covspectrum.util.PangolinLineageAliasResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.javatuples.Pair;
import org.jooq.*;
import org.jooq.covspectrum.Tables;
import org.jooq.impl.DSL;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.beans.PropertyVetoException;
import java.sql.Date;
import java.sql.*;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class DatabaseService {

    public static final String BSSE = "Department of Biosystems Science and Engineering, ETH Zürich";
    public static final String HUG = "HUG, Laboratory of Virology and the Health2030 Genome Center";
    private static final ComboPooledDataSource pool = new ComboPooledDataSource();

    static {
        try {
            pool.setDriverClass("org.postgresql.Driver");
            pool.setJdbcUrl("jdbc:postgresql://" + System.getenv("COV_SPECTRUM_HOST") + ":" +
                    System.getenv("COV_SPECTRUM_PORT") + "/" + System.getenv("COV_SPECTRUM_NAME"));
            pool.setUser(System.getenv("COV_SPECTRUM_USERNAME"));
            pool.setPassword(System.getenv("COV_SPECTRUM_PASSWORD"));
        } catch (PropertyVetoException e) {
            throw new RuntimeException(e);
        }
    }

    private final ObjectMapper objectMapper;

    private final PangolinLineageAliasResolver pangolinLineageAliasResolver;

    public DatabaseService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        try {
            // TODO This will be only loaded once and will not reload when the aliases change. The aliases should not
            //   change too often so it is not a very big issue but it could potentially cause unexpected results.
            this.pangolinLineageAliasResolver = new PangolinLineageAliasResolver(getPangolinLineageAliases());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public Connection getDatabaseConnection() throws SQLException {
        return pool.getConnection();
    }


    public DSLContext getDSLCtx(Connection connection) {
        return DSL.using(connection, SQLDialect.POSTGRES);
    }


    public List<String> getCountryNames() throws SQLException {
        try (Connection conn = getDatabaseConnection()) {
            DSLContext ctx = getDSLCtx(conn);
            Table<?> metaTbl = getMetaTable(ctx, new SampleSelection().setUsePrivate(false));
            var statement = ctx
                    .selectDistinct(MyDSL.fCountry(metaTbl))
                    .from(metaTbl)
                    .orderBy(MyDSL.fCountry(metaTbl));
            return statement.fetch()
                    .map(Record1::value1);
        }
    }


    public List<PangolinLineageAlias> getPangolinLineageAliases() throws SQLException {
        String sql = """
                select
                  alias,
                  full_name
                from pangolin_lineage_alias;
        """;
        try (Connection conn = getDatabaseConnection()) {
            try (Statement statement = conn.createStatement()) {
                try (ResultSet rs = statement.executeQuery(sql)) {
                    List<PangolinLineageAlias> aliases = new ArrayList<>();
                    while (rs.next()) {
                        aliases.add(new PangolinLineageAlias(
                                rs.getString("alias"),
                                rs.getString("full_name")
                        ));
                    }
                    return aliases;
                }
            }
        }
    }


    public List<Distribution<LocalDate, CountAndProportionWithCI>> getDailyTimeDistribution(
            Variant variant,
            String region,
            String country,
            float matchPercentage,
            String pangolinLineage,
            DataType dataType,
            LocalDate fromDate,
            LocalDate endDate
    ) throws SQLException {
        SampleSelection selection = new SampleSelection()
                .setUsePrivate(false).setVariant(variant).setMatchPercentage(matchPercentage)
                .setPangolinLineage(pangolinLineage).setRegion(region).setCountry(country)
                .setDateFrom(fromDate).setDateTo(endDate).setDataType(dataType);
        try (Connection conn = getDatabaseConnection()) {
            DSLContext ctx = getDSLCtx(conn);
            Table<?> matchedSequences = getMetaTable(ctx, selection);
            Table<?> metaTbl = getMetaTable(ctx, new SampleSelection()
                    .setUsePrivate(false).setRegion(region).setCountry(country)
                    .setDateFrom(fromDate).setDateTo(endDate).setDataType(dataType));
            Table<?> countPerDate = ctx
                    .select(
                            MyDSL.fDate(metaTbl),
                            DSL.count().as("count")
                    )
                    .from(metaTbl)
                    .groupBy(MyDSL.fDate(metaTbl))
                    .asTable();
            var statement = ctx
                    .select(
                            MyDSL.fDate(countPerDate),
                            DSL.sum(
                                    DSL.when(MyDSL.fSequenceName(matchedSequences).isNotNull(), 1)
                                    .otherwise(0)
                            ).cast(Integer.class).as("count"),
                            MyDSL.fCount(countPerDate).as("total")
                    )
                    .from(countPerDate.leftJoin(matchedSequences).on(
                            MyDSL.fDate(countPerDate).eq(MyDSL.fDate(matchedSequences))))
                    .groupBy(
                            MyDSL.fDate(countPerDate),
                            MyDSL.fCount(countPerDate)
                    )
                    .orderBy(MyDSL.fDate(countPerDate));
            return statement.fetch()
                    .map(r -> new Distribution<>(
                            r.value1(),
                            CountAndProportionWithCI.fromWilsonCI(r.value2(), r.value3())
                    ));
        }
    }


    public List<SampleFull> getSamples(
            Variant variant,
            String pangolinLineage,
            String country,
            float matchPercentage,
            boolean usePrivateVersion,
            DataType dataType
    ) throws SQLException {
        SampleSelection selection = new SampleSelection()
                .setUsePrivate(usePrivateVersion).setVariant(variant).setMatchPercentage(matchPercentage)
                .setPangolinLineage(pangolinLineage).setCountry(country).setDataType(dataType);
        try (Connection conn = getDatabaseConnection()) {
            DSLContext ctx = getDSLCtx(conn);
            Table<?> mutTbl = getMutTable(ctx, usePrivateVersion);
            var sequences = getMetaTable(ctx, selection).as("meta");
            var statement = ctx
                    .select(
                            MyDSL.fSequenceName(sequences),
                            MyDSL.fDate(sequences),
                            MyDSL.fRegion(sequences),
                            MyDSL.fCountry(sequences),
                            MyDSL.fDivision(sequences),
                            MyDSL.fLocation(sequences),
                            MyDSL.fZipCode(sequences),
                            MyDSL.fHost(sequences),
                            MyDSL.fAge(sequences),
                            MyDSL.fSex(sequences),
                            MyDSL.fSubmittingLab(sequences),
                            MyDSL.fOriginatingLab(sequences),
                            MyDSL.fAgeGroup(sequences),
                            DSL.groupConcat(MyDSL.fAaMutation(mutTbl)).separator(",").as("mutations")
                    )
                    .from(MyDSL.metaJoinMut(sequences, mutTbl))
                    .groupBy(
                            MyDSL.fSequenceName(sequences),
                            MyDSL.fDate(sequences),
                            MyDSL.fRegion(sequences),
                            MyDSL.fCountry(sequences),
                            MyDSL.fDivision(sequences),
                            MyDSL.fLocation(sequences),
                            MyDSL.fZipCode(sequences),
                            MyDSL.fHost(sequences),
                            MyDSL.fAge(sequences),
                            MyDSL.fSex(sequences),
                            MyDSL.fSubmittingLab(sequences),
                            MyDSL.fOriginatingLab(sequences),
                            MyDSL.fAgeGroup(sequences)
                    );
            List<SampleFull> result = new ArrayList<>();
            for (var r : statement.fetch()) {
                List<AAMutation> ms = Arrays.stream(r.get("mutations", String.class).split(","))
                        .map(AAMutation::new).collect(Collectors.toList());
                SamplePrivateMetadata privateMetadata = null;
                if (usePrivateVersion) {
                    privateMetadata = new SamplePrivateMetadata(
                            r.get("country", String.class),
                            r.get("division", String.class),
                            r.get("location", String.class),
                            r.get("zip_code", String.class),
                            r.get("host", String.class),
                            r.get("age", Integer.class),
                            r.get("sex", String.class),
                            r.get("submitting_lab", String.class),
                            r.get("originating_lab", String.class)
                    );
                }
                if (!usePrivateVersion && !BSSE.equals(r.get("submitting_lab", String.class))) {
                    ms = null;
                }
                SampleFull s = new SampleFull(
                        r.get("sequence_name", String.class),
                        r.get("country", String.class),
                        r.get("date", LocalDate.class),
                        ms,
                        privateMetadata
                );
                result.add(s);
            }
            return result;
        }
    }


    public WeightedSampleResultSet getSamples2(
            SampleSelection selection,
            Collection<String> fields
    ) throws SQLException {
        Map<String, Pair<String, Class<?>>> ALL_FIELDS = new HashMap<>() {{
            put("date", new Pair<>("date", LocalDate.class));
            put("region", new Pair<>("region", String.class));
            put("country", new Pair<>("country", String.class));
            put("division", new Pair<>("division", String.class));
            put("zipCode", new Pair<>("zip_code", String.class));
            put("ageGroup", new Pair<>("age_group", String.class));
            put("sex", new Pair<>("sex", String.class));
            put("hospitalized", new Pair<>("hospitalized", Boolean.class));
            put("deceased", new Pair<>("deceased", Boolean.class));
            put("pangolinLineage", new Pair<>("pangolin_lineage", String.class));
        }};
        try (Connection conn = getDatabaseConnection()) {
            DSLContext ctx = getDSLCtx(conn);
            var samples = getMetaTable(ctx, selection).as("meta");
            List<Field<?>> groupByFields = fields.stream()
                    .map(name -> samples.field(ALL_FIELDS.get(name).getValue0(), ALL_FIELDS.get(name).getValue1()))
                    .collect(Collectors.toList());
            List<Field<?>> selectFields = new ArrayList<>(groupByFields);
            selectFields.add(DSL.count().as("count"));
            var statement = ctx
                    .select(selectFields)
                    .from(samples)
                    .groupBy(groupByFields);
            List<WeightedSample> results = statement.fetch()
                    .map(r -> {
                        LocalDate _date = null;
                        String _region = null;
                        String _country = null;
                        String _division = null;
                        String _zipCode = null;
                        String _ageGroup = null;
                        String _sex = null;
                        Boolean _hospitalized = null;
                        Boolean _deceased = null;
                        String _pangolinLineage = null;
                        int count = r.get("count", Integer.class);
                        if (fields.contains("date")) {
                            _date = r.get("date", LocalDate.class);
                        }
                        if (fields.contains("region")) {
                            _region = r.get("region", String.class);
                        }
                        if (fields.contains("country")) {
                            _country = r.get("country", String.class);
                        }
                        if (fields.contains("division")) {
                            _division = r.get("division", String.class);
                        }
                        if (fields.contains("zipCode")) {
                            _zipCode = r.get("zip_code", String.class);
                        }
                        if (fields.contains("ageGroup")) {
                            _ageGroup = r.get("age_group", String.class);
                        }
                        if (fields.contains("sex")) {
                            _sex = r.get("sex", String.class);
                        }
                        if (fields.contains("hospitalized")) {
                            _hospitalized = r.get("hospitalized", Boolean.class);
                        }
                        if (fields.contains("deceased")) {
                            _deceased = r.get("deceased", Boolean.class);
                        }
                        if (fields.contains("pangolinLineage")) {
                            _pangolinLineage = r.get("pangolin_lineage", String.class);
                        }
                        return new WeightedSample(_date, _region, _country, _division, _zipCode, _ageGroup, _sex,
                                _hospitalized, _deceased, _pangolinLineage, count);
                    });
            return new WeightedSampleResultSet(new ArrayList<>(fields), results);
        }
    }


    public List<SampleSequence> getSampleSequences(
            List<SampleName> sampleNames,
            boolean usePrivateVersion
    ) throws SQLException {
        // If the sample name begins with "UNRELEASED_ETHZ_", the sequence has to be looked up in consensus_sequence,
        // otherwise, it is in gisaid_sequence.
        List<Integer> ethids = new ArrayList<>();
        List<String> gisaid_epi_isls = new ArrayList<>();
        for (SampleName sampleName : sampleNames) {
            String s = sampleName.getName();
            if (s.startsWith("UNRELEASED_ETHZ_")) {
                ethids.add(Integer.parseInt(s.substring(16)));
            } else {
                gisaid_epi_isls.add(s);
            }
        }

        String sql;
        if (usePrivateVersion) {
            sql = """
            select
              'UNRELEASED_ETHZ_' || cs.ethid as sample_name,
              cs.seq as sequence
            from consensus_sequence cs
            where ethid = any(?::int[])
            union all
            select
              gs.gisaid_epi_isl as sample_name,
              gs.seq_original as sequence
            from gisaid_api_sequence gs
            where gisaid_epi_isl = any(?::text[]);
        """;
        } else {
            sql = """
            select
              'UNRELEASED_ETHZ_' || cs.ethid as sample_name,
              cs.seq as sequence
            from consensus_sequence cs
            where ethid = any(?::int[])
            union all
            select
              gs.gisaid_epi_isl as sample_name,
              gs.seq_original as sequence
            from gisaid_api_sequence gs
            where
                gisaid_epi_isl = any(?::text[])
                and strain like '%-ETHZ-%';
        """;
        }
        try (Connection conn = getDatabaseConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setArray(1, conn.createArrayOf("int", ethids.toArray()));
            statement.setArray(2, conn.createArrayOf("text", gisaid_epi_isls.toArray()));
            try (ResultSet rs = statement.executeQuery()) {
                List<SampleSequence> result = new ArrayList<>();
                while (rs.next()) {
                    SampleSequence s = new SampleSequence(
                            rs.getString("sample_name"),
                            rs.getString("sequence")
                    );
                    result.add(s);
                }
                return result;
            }
        }
    }


    public List<Distribution<LocalDate, CasesAndSequences>> getTimeIntensityDistribution(
            String region,
            String country,
            DataType dataType
    ) throws SQLException {
        String sql = """
            select
              date,
              region,
              country,
              cases,
              sequenced,
              sequenced_surveillance
            from spectrum_sequence_intensity
            where date is not null and
        """;
        if (country == null && region == null) {
            sql += " country is null and region is null\n";
        } else if (country == null) {
            sql += " country is null and region = ?\n";
        } else {
            sql += " country = ?\n";
        }
        sql += "order by date;";
        try (Connection conn = getDatabaseConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            if (country == null && region == null) {
                // No params
            } else if (country == null) {
                statement.setString(1, region);
            } else {
                statement.setString(1, country);
            }
            try (ResultSet rs = statement.executeQuery()) {
                List<Distribution<LocalDate, CasesAndSequences>> result = new ArrayList<>();
                while (rs.next()) {
                    Distribution<LocalDate, CasesAndSequences> d = new Distribution<>(
                            rs.getDate("date").toLocalDate(),
                            new CasesAndSequences(
                                    rs.getInt("cases"),
                                    rs.getInt(dataType == DataType.SURVEILLANCE ? "sequenced_surveillance" : "sequenced")
                            )
                    );
                    result.add(d);
                }
                return result;
            }
        }
    }


    public String getPrecomputedInterestingVariants(
            String country,
            @Nullable DataType dataType
    ) throws SQLException {
        // TODO Use datatype
        String sql = """
            select result
            from spectrum_new_interesting_variant
            where country = ?
            order by insertion_timestamp desc
            limit 1;
        """;
        try (Connection conn = getDatabaseConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, country);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("result");
                } else {
                    return null;
                }
            }
        }
    }


    private Table<?> getMetaTable(DSLContext ctx, SampleSelection selection) {
        boolean usePrivate = selection.isUsePrivate();
        SpectrumMetadataTable table = !usePrivate ? SpectrumMetadataTable.PUBLIC : SpectrumMetadataTable.PRIVATE;
        Table<?> metaTbl = ctx
                .select(
                        table.asterisk(),
                        DSL
                                .when(table.AGE.lt(10), "0-9")
                                .when(table.AGE.lt(20), "10-19")
                                .when(table.AGE.lt(30), "20-29")
                                .when(table.AGE.lt(40), "30-39")
                                .when(table.AGE.lt(50), "40-49")
                                .when(table.AGE.lt(60), "50-59")
                                .when(table.AGE.lt(70), "60-69")
                                .when(table.AGE.lt(80), "70-79")
                                .when(table.AGE.ge(80), "80+").as("age_group")
                )
                .from(table)
                .where(table.DATE.isNotNull())
                .asTable("spectrum_metadata");

        List<Condition> conditions = new ArrayList<>();

        Table<?> mutTbl = null;
        if (selection.getVariant() != null) {
            mutTbl = getMutTable(ctx, selection.isUsePrivate());
            Condition c = DSL.falseCondition();
            for (AAMutation mutation : selection.getVariant().getMutations()) {
                c = c.or(MyDSL.aaMutationEq(mutTbl, mutation));
            }
            conditions.add(c);
        }
        String pangolinLineage = selection.getPangolinLineage();
        if (pangolinLineage != null) {
            String[] pangolinLineageLikeStatements = parsePangolinLineageQuery(pangolinLineage);
            conditions.add(MyDSL.fPangolinLineage(metaTbl).like(DSL.any(pangolinLineageLikeStatements)));
        }
        if (selection.getRegion() != null) {
            conditions.add(MyDSL.fRegion(metaTbl).eq(selection.getRegion()));
        }
        if (selection.getCountry() != null) {
            conditions.add(MyDSL.countryConstantEq(metaTbl, selection.getCountry()));
        }
        if (selection.getDataType() != null) {
            if (selection.getDataType() == DataType.SURVEILLANCE) {
                conditions.add(MyDSL.fSubmittingLab(metaTbl).eq(BSSE)
                        .or(MyDSL.fSubmittingLab(metaTbl).eq(HUG)));
            }
        }
        if (selection.getDateFrom() != null) {
            conditions.add(MyDSL.fDate(metaTbl).ge(selection.getDateFrom()));
        }
        if (selection.getDateTo() != null) {
            conditions.add(MyDSL.fDate(metaTbl).le(selection.getDateTo()));
        }
        List<Field<?>> fields = Arrays.asList(
                MyDSL.fSequenceName(metaTbl),
                MyDSL.fDate(metaTbl),
                MyDSL.fRegion(metaTbl),
                MyDSL.fCountry(metaTbl),
                MyDSL.fDivision(metaTbl),
                MyDSL.fLocation(metaTbl),
                MyDSL.fZipCode(metaTbl),
                MyDSL.fHost(metaTbl),
                MyDSL.fAge(metaTbl),
                MyDSL.fSex(metaTbl),
                MyDSL.fSubmittingLab(metaTbl),
                MyDSL.fOriginatingLab(metaTbl),
                MyDSL.fHospitalized(metaTbl),
                MyDSL.fDeceased(metaTbl),
                MyDSL.fAgeGroup(metaTbl),
                MyDSL.fPangolinLineage(metaTbl)
        );
        if (selection.getVariant() == null) {
            return ctx.
                    select(fields)
                    .from(metaTbl)
                    .where(conditions)
                    .asTable();
        } else {
            return ctx.
                    select(fields)
                    .from(MyDSL.metaJoinMut(metaTbl, mutTbl))
                    .where(conditions)
                    .groupBy(fields)
                    .having(DSL.count().ge((int) Math.ceil(
                            selection.getMatchPercentage() * selection.getVariant().getMutations().size())))
                    .asTable();
        }
    }


    private Table<?> getMutTable(DSLContext ctx, boolean usePrivate) {
        return ctx.select().from(Tables.SPECTRUM_SEQUENCE_PUBLIC_MUTATION_AA).asTable();
    }


    public void incrementSampleUsageStatistics(
            SampleSelection selection,
            Collection<String> groupByFieldNames
    ) throws SQLException {
        SampleSelectionCacheKey cacheKey = SampleSelectionCacheKey.fromSampleSelection(selection, groupByFieldNames);
        String sql = """
            insert into spectrum_api_usage_sample as s (
                isoyear, isoweek, usage_count,
                fields, private_version, region, country, mutations, match_percentage, pangolin_lineage,
                data_type, date_from, date_to
            )
            values (
                extract(isoyear from current_date), extract(week from current_date), 1,
                ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
            )
            on conflict on constraint spectrum_api_usage_sample_unique_constraint
              do update
              set usage_count = s.usage_count + 1;
        """;
        try (Connection conn = getDatabaseConnection()) {
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setString(1, cacheKey.getFields());
                statement.setBoolean(2, cacheKey.isPrivateVersion());
                statement.setString(3, cacheKey.getRegion());
                statement.setString(4, cacheKey.getCountry());
                statement.setString(5, cacheKey.getMutations());
                statement.setFloat(6, cacheKey.getMatchPercentage());
                statement.setString(7, cacheKey.getPangolinLineage());
                statement.setString(8, cacheKey.getDataType());
                statement.setDate(9, Date.valueOf(cacheKey.getDateFrom()));
                statement.setDate(10, Date.valueOf(cacheKey.getDateTo()));
                statement.execute();
            }
        }
    }


    public String fetchSamplesFromCache(
            SampleSelection selection,
            Collection<String> groupByFieldNames
    ) throws SQLException {
        SampleSelectionCacheKey cacheKey = SampleSelectionCacheKey.fromSampleSelection(selection, groupByFieldNames);
        String sql = """
            select cache
            from spectrum_api_cache_sample c
            where
              c.fields = ?
              and c.private_version = ?
              and c.region = ?
              and c.country = ?
              and c.mutations = ?
              and c.match_percentage = ?
              and c.pangolin_lineage = ?
              and c.data_type = ?
              and c.date_from = ?
              and c.date_to = ?;
        """;
        try (Connection conn = getDatabaseConnection()) {
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setString(1, cacheKey.getFields());
                statement.setBoolean(2, cacheKey.isPrivateVersion());
                statement.setString(3, cacheKey.getRegion());
                statement.setString(4, cacheKey.getCountry());
                statement.setString(5, cacheKey.getMutations());
                statement.setFloat(6, cacheKey.getMatchPercentage());
                statement.setString(7, cacheKey.getPangolinLineage());
                statement.setString(8, cacheKey.getDataType());
                statement.setDate(9, Date.valueOf(cacheKey.getDateFrom()));
                statement.setDate(10, Date.valueOf(cacheKey.getDateTo()));
                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("cache");
                    } else {
                        return null;
                    }
                }
            }
        }
    }


    public String getWasteWaterResults(
            String country
    ) throws SQLException {
        if (!country.equals("Switzerland")) {
            return "null";
        }

        String sql = """
            select
              jsonb_build_object(
                'data', json_agg(
                  json_build_object(
                    'variantName', ww.variant_name,
                    'location', ww.location,
                    'data', ww.data
                  ))
                ) as data
            from spectrum_waste_water_result ww;
        """;
        try (Connection conn = getDatabaseConnection()) {
            try (Statement statement = conn.createStatement()) {
                try (ResultSet rs = statement.executeQuery(sql)) {
                    rs.next();
                    return rs.getString("data");
                }
            }
        }
    }


    public PangolinLineageResponse getPangolinLineageInformation(
            String name,
            String region,
            String country,
            LocalDate dateFrom,
            LocalDate dateTo
    ) throws SQLException {
        String[] mutationTypes = new String[]{"aa", "nuc"};
        PangolinLineageResponse pangolinLineageResponse = new PangolinLineageResponse();
        for (String mutationType : mutationTypes) {
            // TODO Rewrite with jooq
            String sqlConditions1;
            String sqlConditions2;
            int preparedStatementArgumentPairs = 0;
            sqlConditions1 = "m.pangolin_lineage like any(?)";
            sqlConditions2 = "plm.pangolin_lineage like any(?)";
            preparedStatementArgumentPairs++;
            if (region != null) {
                sqlConditions1 += " and m.region = ?";
                sqlConditions2 += " and plm.region = ?";
                preparedStatementArgumentPairs++;
            }
            if (country != null) {
                sqlConditions1 += " and m.country = ?";
                sqlConditions2 += " and plm.country = ?";
                preparedStatementArgumentPairs++;
            }
            if (dateFrom != null) {
                sqlConditions1 += " and m.date >= ?";
                sqlConditions2 += " and plm.date >= ?";
                preparedStatementArgumentPairs++;
            }
            if (dateTo != null) {
                sqlConditions1 += " and m.date <= ?";
                sqlConditions2 += " and plm.date <= ?";
                preparedStatementArgumentPairs++;
            }
            String sql = """
                select
                  x.aa_mutation as mutation,
                  x.count,
                  x.proportion
                from
                  (
                    select
                      plm.aa_mutation,
                      sum(plm.count) as count,
                      sum(plm.count) * 1.0 / (
                        select count(*)
                        from spectrum_sequence_public_meta m
                        where
                        """ + sqlConditions1 + """
                      ) as proportion
                    from spectrum_pangolin_lineage_mutation plm
                    where
                      """ + sqlConditions2 + """
                        group by plm.aa_mutation
                  ) x
                where x.proportion >= 0.2
                order by x.proportion desc;
            """;
            if (mutationType.equals("nuc")) {
                sql = sql
                        .replaceAll("spectrum_pangolin_lineage_mutation", "spectrum_pangolin_lineage_mutation_nucleotide")
                        .replaceAll("aa_mutation", "nuc_mutation");
            }
            try (Connection conn = getDatabaseConnection()) {
                try (PreparedStatement statement = conn.prepareStatement(sql)) {
                    int i = 1;
                    String[] parsedPL = parsePangolinLineageQuery(name);
                    statement.setArray(1, conn.createArrayOf("text", parsedPL));
                    statement.setArray(1 + preparedStatementArgumentPairs, conn.createArrayOf("text", parsedPL));
                    i++;
                    if (region != null) {
                        statement.setString(i, region);
                        statement.setString(i + preparedStatementArgumentPairs, region);
                        i++;
                    }
                    if (country != null) {
                        statement.setString(i, country);
                        statement.setString(i + preparedStatementArgumentPairs, country);
                        i++;
                    }
                    if (dateFrom != null) {
                        statement.setDate(i, Date.valueOf(dateFrom));
                        statement.setDate(i + preparedStatementArgumentPairs, Date.valueOf(dateFrom));
                        i++;
                    }
                    if (dateTo != null) {
                        statement.setDate(i, Date.valueOf(dateTo));
                        statement.setDate(i + preparedStatementArgumentPairs, Date.valueOf(dateTo));
                        i++;
                    }
                    try (ResultSet rs = statement.executeQuery()) {
                        List<MutationCount> mutationCounts = new ArrayList<>();
                        while (rs.next()) {
                            mutationCounts.add(new MutationCount()
                                    .setMutation(rs.getString("mutation"))
                                    .setCount(rs.getInt("count"))
                                    .setProportion(rs.getFloat("proportion")));
                        }
                        if (mutationType.equals("aa")) {
                            pangolinLineageResponse.setCommonMutations(mutationCounts);
                        } else {
                            pangolinLineageResponse.setCommonNucMutations(mutationCounts);
                        }
                    }
                }
            }
        }
        return pangolinLineageResponse;
    }


    /**
     * This function translates a pangolin lineage query to an array of SQL like-statements. A sequence matches the
     * query if any like-statements are fulfilled. The like-statements are designed to be passed into the following
     * SQL statement:
     *   where pangolin_lineage like any(?)
     *
     * Prefix search: Return the lineage and all sub-lineages. I.e., for both "B.1.*" and "B.1*", B.1 and
     * all lineages starting with "B.1." should be returned. "B.11" should not be returned.
     *
     * Example: "B.1.2*" will return [B.1.2, B.1.2.%].
     */
    private String[] parsePangolinLineageQuery(String query) {
        String finalQuery = query.toUpperCase();

        // Resolve aliases
        List<String> resolvedQueries = new ArrayList<>() {{
            add(finalQuery);
        }};
        resolvedQueries.addAll(pangolinLineageAliasResolver.findAlias(query));

        // Handle prefix search
        List<String> result = new ArrayList<>();
        for (String resolvedQuery : resolvedQueries) {
            if (resolvedQuery.contains("%")) {
                // Nope, I don't want to allow undocumented features.
            } else if (!resolvedQuery.endsWith("*")) {
                result.add(resolvedQuery);
            } else {
                // Prefix search
                String rootLineage = resolvedQuery.substring(0, resolvedQuery.length() - 1);
                if (rootLineage.endsWith(".")) {
                    rootLineage = rootLineage.substring(0, rootLineage.length() - 1);
                }
                String subLineages = rootLineage + ".%";
                result.add(rootLineage);
                result.add(subLineages);
            }
        }
        return result.toArray(new String[0]);
    }


    public List<RxivArticle> getPangolinLineageArticles(String pangolinLineage) throws SQLException {
        List<RxivArticle> articles = new ArrayList<>();
        String sql = """
            select
              rar.doi,
              rar.title,
              string_agg(rau.name, '|' order by rara.position) as authors,
              rar.date,
              rar.category,
              rar.published,
              rar.server,
              rar.abstract
            from
              pangolin_lineage__rxiv_article plrar
              join rxiv_article rar on plrar.doi = rar.doi
              left join rxiv_article__rxiv_author rara on rar.doi = rara.doi
              join rxiv_author rau on rara.author_id = rau.id
            where plrar.pangolin_lineage like any(?)
            group by rar.doi, rar.date
            order by rar.date desc;
        """;
        try (Connection conn = getDatabaseConnection()) {
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                String[] parsedPL = parsePangolinLineageQuery(pangolinLineage);
                statement.setArray(1, conn.createArrayOf("text", parsedPL));
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        RxivArticle article = new RxivArticle()
                                .setDoi(rs.getString("doi"))
                                .setTitle(rs.getString("title"))
                                .setAuthors(Arrays.asList(rs.getString("authors").split("\\|")))
                                .setDate(rs.getDate("date").toLocalDate())
                                .setCategory(rs.getString("category"))
                                .setPublished(rs.getString("published"))
                                .setServer(rs.getString("server"))
                                .setAbstractText(rs.getString("abstract"));
                        articles.add(article);
                    }
                }
            }
        }
        return articles;
    }


    public List<CaseCounts> getSwissCaseCounts(LocalDate dateFrom, LocalDate dateTo) throws SQLException {
        String conditionSql = " true";
        if (dateFrom != null) {
            conditionSql += " and date >= ?";
        }
        if (dateTo != null) {
            conditionSql += " and date <= ?";
        }
        String sql = """
            select
              division,
              age_group,
              sex,
              hospitalized,
              deceased,
              sum(count) as count
            from
              (
                select
                  division,
                  (case
                    when age < 10 then '0-9'
                    when age between 10 and 19 then '10-19'
                    when age between 20 and 29 then '20-29'
                    when age between 30 and 39 then '30-39'
                    when age between 40 and 49 then '40-49'
                    when age between 50 and 59 then '50-59'
                    when age between 60 and 69 then '60-69'
                    when age between 70 and 79 then '70-79'
                    when age >= 80 then '80+'
                  end) as age_group,
                  sex,
                  hospitalized,
                  deceased,
                  count
                from spectrum_swiss_cases
                where""" + conditionSql + """
              ) x
            group by division, age_group, sex, hospitalized, deceased
            order by division, age_group, sex, hospitalized, deceased;
        """;
        List<CaseCounts> result = new ArrayList<>();
        try (Connection conn = getDatabaseConnection()) {
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                int paramIndex = 1;
                if (dateFrom != null) {
                    statement.setDate(paramIndex++, Date.valueOf(dateFrom));
                }
                if (dateTo != null) {
                    statement.setDate(paramIndex++, Date.valueOf(dateTo));
                }
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        CaseCounts caseCounts = new CaseCounts()
                                .setDivision(rs.getString("division"))
                                .setAgeGroup(rs.getString("age_group"))
                                .setSex(rs.getString("sex"))
                                .setHospitalized(rs.getBoolean("hospitalized"))
                                .setDeceased(rs.getBoolean("deceased"))
                                .setCount(rs.getInt("count"));
                        result.add(caseCounts);
                    }
                }
            }
        }
        return result;
    }


    public DataStatus getDataStatus() throws SQLException {
        String sql = """
            select state::timestamp as timestamp
            from automation_state
            where program_name = 'refresh_all_mv()';
        """;
        try (Connection conn = getDatabaseConnection()) {
            try (Statement statement = conn.createStatement()) {
                try (ResultSet rs = statement.executeQuery(sql)) {
                    rs.next();
                    return new DataStatus(rs.getTimestamp("timestamp").toLocalDateTime());
                }
            }
        }
    }
}
