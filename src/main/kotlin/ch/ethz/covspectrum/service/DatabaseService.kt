package ch.ethz.covspectrum.service

import ch.ethz.covspectrum.entity.req.CaseAggregationField
import ch.ethz.covspectrum.entity.req.CaseAggregationRequest
import ch.ethz.covspectrum.entity.res.*
import ch.ethz.covspectrum.util.JooqHelper
import ch.ethz.covspectrum.util.PangoLineageAlias
import ch.ethz.covspectrum.util.PangoLineageQueryToSqlLikesConverter
import com.mchange.v2.c3p0.ComboPooledDataSource
import org.jooq.Condition
import org.jooq.Field
import org.jooq.covspectrum.tables.SpectrumCases
import org.jooq.impl.DSL
import org.springframework.stereotype.Service
import java.sql.Connection


private val pool: ComboPooledDataSource = ComboPooledDataSource().apply {
    driverClass = "org.postgresql.Driver"
    jdbcUrl = "jdbc:postgresql://" + System.getenv("COV_SPECTRUM_DB_HOST") + ":" +
        System.getenv("COV_SPECTRUM_DB_PORT") + "/" + System.getenv("COV_SPECTRUM_DB_NAME")
    user = System.getenv("COV_SPECTRUM_DB_USERNAME")
    password = System.getenv("COV_SPECTRUM_DB_PASSWORD")
}


@Service
class DatabaseService {

    private val pangoLineageQueryToSqlLikesConverter = PangoLineageQueryToSqlLikesConverter(getPangolinLineageAliases())


    fun getConnection(): Connection {
        return pool.connection
    }


    fun getPangolinLineageAliases(): List<PangoLineageAlias> {
        val sql = """
            select
              alias,
              full_name
            from pangolin_lineage_alias;
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


    fun getCountryMapping(): List<CountryMappingResponseEntry> {
        val sql = """
            select
              cov_spectrum_country,
              cov_spectrum_region,
              gisaid_country
            from spectrum_country_mapping
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
            val tbl = SpectrumCases.SPECTRUM_CASES

            val fields = req.fields ?: emptyList()
            val groupByFields = fields.map {
                when (it) {
                    CaseAggregationField.REGION -> tbl.REGION
                    CaseAggregationField.COUNTRY -> tbl.COUNTRY
                    CaseAggregationField.DIVISION -> tbl.DIVISION
                    CaseAggregationField.DATE -> tbl.DATE
                    CaseAggregationField.AGE -> tbl.AGE
                    CaseAggregationField.SEX -> tbl.SEX
                    CaseAggregationField.HOSPITALIZED -> tbl.HOSPITALIZED
                    CaseAggregationField.DIED -> tbl.DIED
                }
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
                    if (fields.contains(CaseAggregationField.AGE)) it.get(tbl.AGE) else null,
                    if (fields.contains(CaseAggregationField.SEX)) it.get(tbl.SEX) else null,
                    if (fields.contains(CaseAggregationField.HOSPITALIZED)) it.get(tbl.HOSPITALIZED) else null,
                    if (fields.contains(CaseAggregationField.DIED)) it.get(tbl.DIED) else null,
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
            from consensus_sequence
            where sample_name = 'REFERENCE_GENOME';
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


    fun getPangoLineageArticles(pangoLineageQuery: String): List<RxivArticleResponseEntry> {
        val sql = """
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
        """.trimIndent()
        getConnection().use { conn ->
            conn.prepareStatement(sql).use { statement ->
                val pangoLineageSqlLikes = pangoLineageQueryToSqlLikesConverter.convert(pangoLineageQuery)
                statement.setArray(1, conn.createArrayOf("text", pangoLineageSqlLikes.toTypedArray()))
                statement.executeQuery().use { rs ->
                    val articles: MutableList<RxivArticleResponseEntry> = mutableListOf()
                    while (rs.next()) {
                        articles.add(
                            RxivArticleResponseEntry(
                                rs.getString("doi"),
                                rs.getString("title"),
                                rs.getString("authors").split("|"),
                                rs.getDate("date").toLocalDate(),
                                rs.getString("category"),
                                rs.getString("published"),
                                rs.getString("server"),
                                rs.getString("abstract")
                            )
                        )
                    }
                    return articles
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
            from spectrum_waste_water_result ww;
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

}
