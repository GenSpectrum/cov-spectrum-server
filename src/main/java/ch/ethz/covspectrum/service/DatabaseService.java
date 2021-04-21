package ch.ethz.covspectrum.service;

import ch.ethz.covspectrum.entity.api.CasesAndSequences;
import ch.ethz.covspectrum.entity.api.CountAndProportionWithCI;
import ch.ethz.covspectrum.entity.api.Distribution;
import ch.ethz.covspectrum.entity.core.DataType;
import ch.ethz.covspectrum.entity.core.*;
import ch.ethz.covspectrum.jooq.MyDSL;
import ch.ethz.covspectrum.jooq.SpectrumMetadataTable;
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


    public DatabaseService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
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
              gs.original_seq as sequence
            from gisaid_sequence gs
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
              gs.original_seq as sequence
            from gisaid_sequence gs
            where
                gisaid_epi_isl = any(?::text[])
                and submitting_lab = 'Department of Biosystems Science and Engineering, ETH Zürich';
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
        if (selection.getPangolinLineage() != null) {
            conditions.add(MyDSL.fPangolinLineage(metaTbl).eq(selection.getPangolinLineage()));
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
}
