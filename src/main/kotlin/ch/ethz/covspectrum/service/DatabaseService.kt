package ch.ethz.covspectrum.service

import ch.ethz.covspectrum.entity.req.CaseDailyRequest
import ch.ethz.covspectrum.entity.res.CaseDailyResponseEntry
import ch.ethz.covspectrum.entity.res.CountryMappingResponseEntry
import ch.ethz.covspectrum.entity.res.Gene
import ch.ethz.covspectrum.entity.res.RxivArticleResponseEntry
import ch.ethz.covspectrum.util.JooqHelper
import ch.ethz.covspectrum.util.PangoLineageAlias
import ch.ethz.covspectrum.util.PangoLineageQueryToSqlLikesConverter
import com.mchange.v2.c3p0.ComboPooledDataSource
import org.jooq.Condition
import org.jooq.covspectrum.tables.SpectrumOwidGlobalCases
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
              gisaid_country,
              owid_country
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
                                rs.getString("owid_country"),
                                rs.getString("cov_spectrum_region")
                            )
                        )
                    }
                    return countries;
                }
            }
        }
    }


    fun getDailyCases(req: CaseDailyRequest): List<CaseDailyResponseEntry> {
        getConnection().use { conn ->
            val ctx = JooqHelper.getDSLCtx(conn)
            val tbl = SpectrumOwidGlobalCases.SPECTRUM_OWID_GLOBAL_CASES
            val conditions: List<Condition> = listOfNotNull(
                req.region?.let { tbl.REGION.eq(it) },
                req.country?.let { tbl.COUNTRY.eq(it) }
            )
            val statement = ctx
                .select(tbl.DATE, DSL.sum(tbl.NEW_CASES), DSL.sum(tbl.NEW_DEATHS))
                .from(tbl)
                .where(conditions)
                .groupBy(tbl.DATE)
                .orderBy(tbl.DATE)
            println(statement)
            val records = statement.fetch()
            return records.map {
                CaseDailyResponseEntry(
                    it.get(tbl.DATE),
                    it.get(it.field2()).toInt(),
                    it.get(it.field3()).toInt()
                )
            }
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
                                rs.getString("authors").split("\\|"),
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

}
