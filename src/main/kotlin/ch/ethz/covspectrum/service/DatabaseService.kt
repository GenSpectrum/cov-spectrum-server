package ch.ethz.covspectrum.service

import ch.ethz.covspectrum.entity.SpectrumCollection
import ch.ethz.covspectrum.entity.SpectrumCollectionVariant
import ch.ethz.covspectrum.entity.req.CaseAggregationField
import ch.ethz.covspectrum.entity.req.CaseAggregationRequest
import ch.ethz.covspectrum.entity.res.*
import ch.ethz.covspectrum.util.JooqHelper
import ch.ethz.covspectrum.util.PangoLineageAlias
import com.mchange.v2.c3p0.ComboPooledDataSource
import org.jooq.Condition
import org.jooq.Field
import org.jooq.TableField
import org.jooq.covspectrum.tables.Cases
import org.jooq.covspectrum.tables.records.CasesRecord
import org.jooq.impl.DSL
import org.springframework.stereotype.Service
import java.sql.Connection
import java.sql.Timestamp


fun getDbJdbcUrl(): String {
    return "jdbc:postgresql://" + System.getenv("COV_SPECTRUM_DB_HOST") + ":" +
        System.getenv("COV_SPECTRUM_DB_PORT") + "/" + System.getenv("COV_SPECTRUM_DB_NAME")
}

fun getDbUser(): String {
    return System.getenv("COV_SPECTRUM_DB_USERNAME")
}

fun getDbPassword(): String {
    return System.getenv("COV_SPECTRUM_DB_PASSWORD")
}


private val pool: ComboPooledDataSource = ComboPooledDataSource().apply {
    driverClass = "org.postgresql.Driver"
    jdbcUrl = getDbJdbcUrl()
    user = getDbUser()
    password = getDbPassword()
}


@Service
class DatabaseService {

    private val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    fun getConnection(): Connection {
        return pool.connection
    }


    fun getPangoLineageAliases(): List<PangoLineageAlias> {
        val sql = """
            select
              alias,
              full_name
            from pango_lineage_alias;
        """.trimIndent()
        getConnection().use { conn ->
            conn.createStatement().use { statement ->
                statement.executeQuery(sql).use { rs ->
                    val aliases: MutableList<PangoLineageAlias> = mutableListOf()
                    while (rs.next()) {
                        aliases.add(
                            PangoLineageAlias(rs.getString("alias"), rs.getString("full_name"))
                        )
                    }
                    return aliases
                }
            }
        }
    }


    fun getPangoLineageRecombinants(): List<PangoLineageRecombinant> {
        val sql = """
            select
              name,
              string_agg(parent, ',' order by parent_position) as parents
            from pango_lineage_recombinant
            group by name;
        """.trimIndent()
        getConnection().use { conn ->
            conn.createStatement().use { statement ->
                statement.executeQuery(sql).use { rs ->
                    val recombinants: MutableList<PangoLineageRecombinant> = mutableListOf()
                    while (rs.next()) {
                        recombinants.add(
                            PangoLineageRecombinant(rs.getString("name"), rs.getString("parents").split(","))
                        )
                    }
                    return recombinants
                }
            }
        }
    }


    fun getCountryMapping(): List<CountryMappingResponseEntry> {
        val sql = """
            select
              cov_spectrum_country,
              cov_spectrum_region,
              gisaid_country
            from country_mapping
            where
              cov_spectrum_country is not null
              and cov_spectrum_region is not null
              and gisaid_country is not null;
        """.trimIndent()
        getConnection().use { conn ->
            conn.createStatement().use { statement ->
                statement.executeQuery(sql).use { rs ->
                    val countries: MutableList<CountryMappingResponseEntry> = mutableListOf();
                    while (rs.next()) {
                        countries.add(
                            CountryMappingResponseEntry(
                                rs.getString("cov_spectrum_country"),
                                rs.getString("gisaid_country"),
                                rs.getString("cov_spectrum_region")
                            )
                        )
                    }
                    return countries;
                }
            }
        }
    }


    fun getCases(req: CaseAggregationRequest): CaseAggregationResponse {
        getConnection().use { conn ->
            val ctx = JooqHelper.getDSLCtx(conn)
            val tbl = Cases.CASES

            val fields = req.fields ?: emptyList()
            val groupByFields = mutableListOf<TableField<CasesRecord, *>>()
            for (field in fields) {
                if (field == CaseAggregationField.REGION) groupByFields.add(tbl.REGION)
                if (field == CaseAggregationField.COUNTRY) groupByFields.add(tbl.COUNTRY)
                if (field == CaseAggregationField.DIVISION) groupByFields.add(tbl.DIVISION)
                if (field == CaseAggregationField.DATE) groupByFields.add(tbl.DATE)
                // The fields age, sex, hospitalized and died are ignored for now
            }
            val selectFields: MutableList<Field<*>> = groupByFields.toMutableList()
            selectFields.add(DSL.sum(tbl.NEW_CASES).`as`("new_cases"))
            selectFields.add(DSL.sum(tbl.NEW_DEATHS).`as`("new_deaths"))
            val conditions: List<Condition> = listOfNotNull(
                req.region?.let { tbl.REGION.eq(it) },
                req.country?.let { tbl.COUNTRY.eq(it) },
                req.division?.let { tbl.DIVISION.eq(it) },
                req.dateFrom?.let { tbl.DATE.ge(it) },
                req.dateTo?.let { tbl.DATE.le(it) }
            )
            val statement = ctx
                .select(selectFields)
                .from(tbl)
                .where(conditions)
                .groupBy(groupByFields)
            val records = statement.fetch()
            val entries = records.map {
                CaseAggregationResponseEntry(
                    if (fields.contains(CaseAggregationField.REGION)) it.get(tbl.REGION) else null,
                    if (fields.contains(CaseAggregationField.COUNTRY)) it.get(tbl.COUNTRY) else null,
                    if (fields.contains(CaseAggregationField.DIVISION)) it.get(tbl.DIVISION) else null,
                    if (fields.contains(CaseAggregationField.DATE)) it.get(tbl.DATE) else null,
                    null,
                    null,
                    null,
                    null,
                    it.get("new_cases", Int::class.java),
                    it.get("new_deaths", Int::class.java)
                )
            }
            return CaseAggregationResponse(fields, entries)
        }
    }


    fun getReferenceGenome(): String {
        val sql = """
            select seq
            from reference_genome
            where name = 'REFERENCE_GENOME';
        """.trimIndent()
        getConnection().use { conn ->
            conn.createStatement().use { statement ->
                statement.executeQuery(sql).use { rs ->
                    rs.next();
                    return rs.getString("seq");
                }
            }
        }
    }


    fun getAASeqs(): List<Gene> {
        val sql = """
            select gene, reference_aa_sequence, start_position, end_position
            from gene
            order by start_position;
        """.trimIndent()
        getConnection().use { conn ->
            conn.createStatement().use { statement ->
                statement.executeQuery(sql).use { rs ->
                    val genes: MutableList<Gene> = mutableListOf();
                    while (rs.next()) {
                        genes.add(
                            Gene(
                                rs.getString("gene"),
                                rs.getInt("start_position"),
                                rs.getInt("end_position"),
                                rs.getString("reference_aa_sequence")
                            )
                        )
                    }
                    return genes;
                }
            }
        }
    }


    fun getWastewaterResults(region: String?, country: String?, division: String?): String {
        if (!(region == null || region == "Europe") || country != "Switzerland" || division != null) {
            return "null"
        }
        val sql = """
            select
              jsonb_build_object(
                'data', json_agg(
                  json_build_object(
                    'variantName', ww.variant_name,
                    'location', ww.location,
                    'data', ww.data
                  ))
                ) as data
            from wastewater_result ww;
        """.trimIndent()
        getConnection().use { conn ->
            conn.createStatement().use { statement ->
                statement.executeQuery(sql).use { rs ->
                    rs.next()
                    return rs.getString("data")
                }
            }
        }
    }


    fun getWastewaterVILOCAPlot(): String {
        val sql = """
            select plot_svg
            from wastewater_viloca wv;
        """.trimIndent()
        getConnection().use { conn ->
            conn.createStatement().use { statement ->
                statement.executeQuery(sql).use { rs ->
                    rs.next()
                    return rs.getString("plot_svg")
                }
            }
        }
    }


    fun getHuismanScire2021ReResult(key: String): Pair<Boolean, String?>? {
        val sql = """
            select r.success, r.result
            from model_huisman_scire_2021_re r
            where r.key = ?;
        """.trimIndent()
        getConnection().use { conn ->
            conn.prepareStatement(sql).use { statement ->
                statement.setString(1, key)
                statement.executeQuery().use { rs ->
                    if (rs.next()) {
                        return Pair(
                            rs.getBoolean("success"),
                            rs.getString("result")
                        )
                    }
                    return null
                }
            }
        }
    }


    fun insertHuismanScire2021ReResult(
        key: String,
        calculationDurationSeconds: Int,
        request: String,
        success: Boolean,
        result: String?
    ) {
        val sql = """
            insert into model_huisman_scire_2021_re (key, calculation_date, calculation_duration_seconds, request, success, result)
            values (?, now(), ?, ?, ?, ?);
        """.trimIndent()
        getConnection().use { conn ->
            conn.prepareStatement(sql).use { statement ->
                statement.setString(1, key)
                statement.setInt(2, calculationDurationSeconds)
                statement.setString(3, request)
                statement.setBoolean(4, success)
                statement.setString(5, result)
                statement.execute()
            }
        }
    }


    fun getCollections(fetchVariants: Boolean): List<SpectrumCollection> {
        val sql1 = """
            select id, title, description, maintainers, email
            from collection;
        """.trimIndent()
        val sql2 = """
            select collection_id, query, name, description, highlighted
            from collection_variant;
        """.trimIndent()
        val collections = HashMap<Int, SpectrumCollection>();
        getConnection().use { conn ->
            conn.createStatement().use { statement ->
                statement.executeQuery(sql1).use { rs ->
                    while (rs.next()) {
                        collections[rs.getInt("id")] = SpectrumCollection(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getString("maintainers"),
                            rs.getString("email"),
                            mutableListOf()
                        )
                    }
                }
            }
            if (fetchVariants) {
                conn.createStatement().use { statement ->
                    statement.executeQuery(sql2).use { rs ->
                        while (rs.next()) {
                            collections[rs.getInt("collection_id")]!!.variants.add(
                                SpectrumCollectionVariant(
                                    rs.getString("query"),
                                    rs.getString("name"),
                                    rs.getString("description"),
                                    rs.getBoolean("highlighted")
                                )
                            )
                        }
                    }
                }
            }
        }
        return collections.values.toList();
    }


    fun getCollection(id: Int): SpectrumCollection? {
        val sql1 = """
            select id, title, description, maintainers, email
            from collection
            where id = ?;
        """.trimIndent()
        val sql2 = """
            select collection_id, query, name, description, highlighted
            from collection_variant
            where collection_id = ?;
        """.trimIndent()
        var collection: SpectrumCollection? = null
        getConnection().use { conn ->
            conn.prepareStatement(sql1).use { statement ->
                statement.setInt(1, id)
                statement.executeQuery().use { rs ->
                    if (rs.next()) {
                        collection = SpectrumCollection(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getString("maintainers"),
                            rs.getString("email"),
                            mutableListOf()
                        )
                    }
                }
            }
            if (collection == null) {
                return null;
            }
            conn.prepareStatement(sql2).use { statement ->
                statement.setInt(1, id)
                statement.executeQuery().use { rs ->
                    while (rs.next()) {
                        collection!!.variants.add(
                            SpectrumCollectionVariant(
                                rs.getString("query"),
                                rs.getString("name"),
                                rs.getString("description"),
                                rs.getBoolean("highlighted")
                            )
                        )
                    }
                }
            }
        }
        return collection
    }


    fun validateCollectionAdminKey(id: Int, adminKey: String): Boolean? {
        val sql = """
            select admin_key
            from collection
            where id = ?;
        """.trimIndent()
        getConnection().use { conn ->
            conn.prepareStatement(sql).use { statement ->
                statement.setInt(1, id)
                statement.executeQuery().use { rs ->
                    if (!rs.next()) {
                        return null
                    }
                    return rs.getString("admin_key") == adminKey
                }
            }
        }
    }


    fun insertCollection(collection: SpectrumCollection): Pair<Int, String> {
        val sql1 = """
            insert into collection (
              creation_date,
              last_update_date,
              title,
              description,
              maintainers,
              email,
              admin_key
            )
            values (now(), now(), ?, ?, ?, ?, ?)
            returning id;
        """.trimIndent()
        val sql2 = """
            insert into collection_variant (collection_id, query, name, description, highlighted)
            values (?, ?, ?, ?, ?);
        """.trimIndent()
        // Not the safest random generator but should be good enough for our use case
        val adminKey = (1..21)
            .map { i -> kotlin.random.Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")
        var id: Int
        getConnection().use { conn ->
            conn.autoCommit = false
            conn.prepareStatement(sql1).use { statement ->
                statement.setString(1, collection.title)
                statement.setString(2, collection.description)
                statement.setString(3, collection.maintainers)
                statement.setString(4, collection.email)
                statement.setString(5, adminKey)
                statement.executeQuery().use { rs ->
                    rs.next()
                    id = rs.getInt(1)
                }
            }
            conn.prepareStatement(sql2).use { statement ->
                for (variant in collection.variants) {
                    statement.setInt(1, id)
                    statement.setString(2, variant.query)
                    statement.setString(3, variant.name)
                    statement.setString(4, variant.description)
                    statement.setBoolean(5, variant.highlighted)
                    statement.execute()
                }
            }
            conn.commit()
            conn.autoCommit = true
        }
        return Pair(id, adminKey)
    }


    fun updateCollection(collection: SpectrumCollection, adminKey: String) {
        val id = collection.id
        check(id != null)
        val sql0 = """
            delete from collection
            where id = ?
            returning creation_date;
        """.trimIndent()
        val sql1 = """
            insert into collection (
              id,
              creation_date,
              last_update_date,
              title,
              description,
              maintainers,
              email,
              admin_key
            )
            values (?, ?, now(), ?, ?, ?, ?, ?);
        """.trimIndent()
        val sql2 = """
            insert into collection_variant (collection_id, query, name, description, highlighted)
            values (?, ?, ?, ?, ?);
        """.trimIndent()
        var creationDate: Timestamp
        getConnection().use { conn ->
            conn.autoCommit = false
            conn.prepareStatement(sql0).use { statement ->
                statement.setInt(1, id)
                statement.executeQuery().use { rs ->
                    check(rs.next())
                    creationDate = rs.getTimestamp(1)
                }
            }
            conn.prepareStatement(sql1).use { statement ->
                statement.setInt(1, id)
                statement.setTimestamp(2, creationDate)
                statement.setString(3, collection.title)
                statement.setString(4, collection.description)
                statement.setString(5, collection.maintainers)
                statement.setString(6, collection.email)
                statement.setString(7, adminKey)
                statement.execute()
            }
            conn.prepareStatement(sql2).use { statement ->
                for (variant in collection.variants) {
                    statement.setInt(1, id)
                    statement.setString(2, variant.query)
                    statement.setString(3, variant.name)
                    statement.setString(4, variant.description)
                    statement.setBoolean(5, variant.highlighted)
                    statement.execute()
                }
            }
            conn.commit()
            conn.autoCommit = true
        }
    }

    fun deleteCollection(id: Int) {
        val sql0 = """
            delete from collection
            where id = ?;
        """.trimIndent()
        getConnection().use { conn ->
            conn.prepareStatement(sql0).use { statement ->
                statement.setInt(1, id)
                statement.execute()
            }
        }
    }
}
