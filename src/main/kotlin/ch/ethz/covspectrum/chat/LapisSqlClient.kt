package ch.ethz.covspectrum.chat

import ch.ethz.covspectrum.chat.LapisSqlQuery.FilterCondition
import ch.ethz.covspectrum.chat.LapisSqlQuery.FilterCondition.Operator
import ch.ethz.covspectrum.entity.model.chen2021fitness.LapisResponse
import net.sf.jsqlparser.expression.Expression
import net.sf.jsqlparser.expression.Function
import net.sf.jsqlparser.expression.LongValue
import net.sf.jsqlparser.expression.StringValue
import net.sf.jsqlparser.expression.operators.conditional.AndExpression
import net.sf.jsqlparser.expression.operators.relational.*
import net.sf.jsqlparser.parser.CCJSqlParserUtil
import net.sf.jsqlparser.schema.Column
import net.sf.jsqlparser.schema.Table
import net.sf.jsqlparser.statement.select.PlainSelect
import net.sf.jsqlparser.statement.select.Select
import net.sf.jsqlparser.statement.select.SelectExpressionItem
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.time.LocalDate
import java.time.format.DateTimeParseException


/**
 * This class provides a very basic SQL interface for LAPIS. It can only use a small subset of LAPIS' feature and
 * supports very few of SQL's feature. On the other hand, it is gaps a few features that LAPIS does not support
 * such as sorting.
 */
class LapisSqlClient(
    private val host: String,
    private val accessKey: String?
) {
    fun execute(sql: String): List<Map<String, String>> {
        val query = parseSql(sql)
        val transformed = validateAndTransformQuery(query, sql)
        return executeQuery(transformed)
    }

    /**
     * Parses a SQL query to a LapisSqlQuery. The function throws an UnsupportedSqlException if the SQL query
     * uses features that a LapisSqlQuery does not support.
     */
    private fun parseSql(sql: String): LapisSqlQuery {
        val lapisSqlQuery = LapisSqlQuery()

        // Check that it is a plain SELECT statement with not more than following top-level keywords:
        // select, from, where, group by, order by, limit
        val statement = CCJSqlParserUtil.parse(sql)
        if (statement !is Select || statement.withItemsList != null || statement.isUsingWithBrackets) {
            throw UnsupportedSqlException(sql)
        }
        val selectBody = statement.selectBody
        if (
            selectBody !is PlainSelect ||
            selectBody.distinct != null ||
            selectBody.intoTables != null ||
            selectBody.joins != null ||
            selectBody.having != null ||
            selectBody.offset != null ||
            selectBody.fetch != null ||
            selectBody.optimizeFor != null ||
            selectBody.skip != null ||
            selectBody.mySqlHintStraightJoin ||
            selectBody.top != null ||
            selectBody.oracleHierarchical != null ||
            selectBody.oracleHint != null ||
            selectBody.isOracleSiblings ||
            selectBody.isForUpdate ||
            selectBody.forUpdateTable != null ||
            selectBody.isSkipLocked ||
            selectBody.isUseBrackets ||
            selectBody.wait != null ||
            selectBody.mySqlSqlCalcFoundRows ||
            selectBody.mySqlSqlCacheFlag != null ||
            selectBody.forXmlPath != null ||
            selectBody.ksqlWindow != null ||
            selectBody.isNoWait ||
            selectBody.isEmitChanges ||
            selectBody.withIsolation != null ||
            selectBody.windowDefinitions != null
        ) {
            throw UnsupportedSqlException(sql)
        }

        // Select items may only contain simple columns and count(*)
        val selectItems = selectBody.selectItems
        for (selectItem in selectItems) {
            if (selectItem !is SelectExpressionItem) {
                throw UnsupportedSqlException(sql)
            }
            val expression = selectItem.expression
            if (expression is Column && expression.table == null) {
                lapisSqlQuery.selectFields.add(expression.columnName)
            } else if (expression is Function && expression.toString() == "count(*)") {
                lapisSqlQuery.selectFields.add("count")
            } else {
                throw UnsupportedSqlException(sql)
            }
        }

        // From must be "lapis" and only that.
        val fromItem = selectBody.fromItem
        if (fromItem !is Table && fromItem.toString() != "lapis") {
            throw UnsupportedSqlException(sql)
        }

        // Check the where conditions: We only support ANDs, =, >=, >, <=, <, and between
        val whereExpressions = mutableListOf<Expression>()
        if (selectBody.where != null) {
            whereExpressions.add(selectBody.where)
        }
        while (whereExpressions.isNotEmpty()) {
            when (val expr = whereExpressions.removeLast()) {
                is AndExpression -> {
                    whereExpressions.add(expr.leftExpression)
                    whereExpressions.add(expr.rightExpression)
                }

                is ComparisonOperator -> {
                    val left = expr.leftExpression
                    val right = expr.rightExpression
                    if (left !is Column || right !is StringValue) {
                        throw UnsupportedSqlException(sql)
                    }
                    val lapisOperator = when (expr) {
                        is EqualsTo -> Operator.EqualsTo
                        is GreaterThan -> Operator.GreaterThan
                        is GreaterThanEquals -> Operator.GreaterThanEqual
                        is MinorThan -> Operator.LessThan
                        is MinorThanEquals -> Operator.LessThanEqual
                        else -> throw UnsupportedSqlException(sql)
                    }
                    lapisSqlQuery.filterConditions.add(
                        FilterCondition(left.columnName, lapisOperator, right.value)
                    )
                }

                is Between -> {
                    val left = expr.leftExpression
                    val rightStart = expr.betweenExpressionStart
                    val rightEnd = expr.betweenExpressionEnd
                    if (left !is Column || rightStart !is StringValue || rightEnd !is StringValue) {
                        throw UnsupportedSqlException(sql)
                    }
                    lapisSqlQuery.filterConditions.add(
                        FilterCondition(
                            left.columnName,
                            Operator.GreaterThanEqual,
                            rightStart.value
                        )
                    )
                    lapisSqlQuery.filterConditions.add(
                        FilterCondition(
                            left.columnName,
                            Operator.LessThanEqual,
                            rightEnd.value
                        )
                    )
                }

                else -> throw UnsupportedSqlException(sql)
            }
        }

        // Group by
        val groupBy = selectBody.groupBy
        if (groupBy != null) {
            if (groupBy.groupByExpressionList !is ExpressionList) {
                throw UnsupportedSqlException(sql)
            }
            val groupByExpressions = groupBy.groupByExpressionList.expressions
            for (groupByExpression in groupByExpressions) {
                if (groupByExpression !is Column) {
                    throw UnsupportedSqlException(sql)
                }
                lapisSqlQuery.groupByFields.add(groupByExpression.columnName)
            }
        }

        // Order by
        val orderByElements = selectBody.orderByElements
        if (orderByElements != null && orderByElements.size > 0) {
            if (orderByElements.size > 1) {
                throw UnsupportedSqlException(sql)
            }
            val orderByElement = orderByElements[0]
            val expr = orderByElement.expression
            if (expr is Column) {
                lapisSqlQuery.orderByField = expr.columnName
            } else if (expr is Function && expr.toString() == "count(*)") {
                lapisSqlQuery.orderByField = "count"
            } else {
                throw UnsupportedSqlException(sql)
            }
            lapisSqlQuery.orderByAsc = orderByElement.isAsc
        }

        // Limit (and no offset)
        val limit = selectBody.limit
        if (limit != null) {
            val rowCount = limit.rowCount
            if (rowCount != null) {
                if (rowCount !is LongValue) {
                    throw UnsupportedSqlException(sql)
                }
                lapisSqlQuery.limit = rowCount.value.toInt()
            }
            val offset = limit.offset
            if (offset != null) {
                throw UnsupportedSqlException(sql)
            }
        }

        return lapisSqlQuery
    }

    /**
     * Checks whether the LapisSqlQuery only uses known fields and the dates are provided in the ISO format. It also
     * transforms the filter conditions to only use EqualsTo, and write `nextcladePangoLineage` for fields that contain
     * the word "lineage". It further transforms the mutation filters from `nuc_123 = 'T'` to `nucMutations = 123T`
     */
    private fun validateAndTransformQuery(query: LapisSqlQuery, sql: String): LapisSqlQuery {
        val transformedQuery = LapisSqlQuery()
        transformedQuery.transformed = true

        // selectFields
        for (field in query.selectFields) {
            when (field) {
                "count" -> {}
                "region" -> transformedQuery.selectFields.add("region")
                "country" -> transformedQuery.selectFields.add("country")
                "date" -> transformedQuery.selectFields.add("date")
                else -> {
                    if (field.contains("lineage", true)) {
                        transformedQuery.selectFields.add("nextcladePangoLineage")
                    } else {
                        throw UnsupportedSqlException(sql)
                    }
                }
            }
            // TODO
            // A small hack that's needed because LAPIS only allows aggregated queries
            if (field != "count" && !query.groupByFields.contains(field)) {
                query.groupByFields.add(field)
            }
        }

        // filterConditions
        val nucMutations = mutableListOf<String>()
        val aaMutations = mutableListOf<String>()
        for (filterCondition in query.filterConditions) {
            val field = filterCondition.field
            if (field == "region" || field == "country") {
                if (filterCondition.comparisonOperator != Operator.EqualsTo) {
                    throw UnsupportedSqlException(sql)
                }
                transformedQuery.filterConditions.add(filterCondition)
            } else if (field.contains("lineage", true)) {
                if (filterCondition.comparisonOperator != Operator.EqualsTo) {
                    throw UnsupportedSqlException(sql)
                }
                transformedQuery.filterConditions.add(
                    FilterCondition(
                        "nextcladePangoLineage",
                        filterCondition.comparisonOperator,
                        filterCondition.value
                    )
                )
            } else if (field == "date") {
                // Parse date
                val parsedDate = try {
                    LocalDate.parse(filterCondition.value)
                } catch (e: DateTimeParseException) {
                    throw UnsupportedSqlException(sql)
                }
                // Transform operators
                when (filterCondition.comparisonOperator) {
                    Operator.EqualsTo -> {
                        transformedQuery.filterConditions
                            .add(FilterCondition("dateFrom", Operator.GreaterThanEqual, parsedDate.toString()))
                        transformedQuery.filterConditions
                            .add(FilterCondition("dateTo", Operator.LessThanEqual, parsedDate.toString()))
                    }

                    Operator.GreaterThan -> transformedQuery.filterConditions
                        .add(FilterCondition("dateFrom", Operator.GreaterThanEqual, parsedDate.plusDays(1).toString()))

                    Operator.GreaterThanEqual -> transformedQuery.filterConditions.add(FilterCondition("dateFrom", Operator.GreaterThanEqual, parsedDate.toString()))
                    Operator.LessThan -> transformedQuery.filterConditions
                        .add(FilterCondition("dateTo", Operator.LessThanEqual, parsedDate.minusDays(1).toString()))

                    Operator.LessThanEqual -> transformedQuery.filterConditions
                        .add(FilterCondition("dateTo", Operator.LessThanEqual, parsedDate.toString()))
                }
            } else if (field.startsWith("nuc_")) {
                val position = field.substring(4).toIntOrNull()
                val value = filterCondition.value
                if (position == null || value.length != 1) {
                    throw UnsupportedSqlException(sql)
                }
                nucMutations.add("${position}${value}")
            } else if (field.startsWith("aa_")) {
                val withoutPrefix = field.substring(3)
                val parts = withoutPrefix.split("_")
                if (parts.size != 2) {
                    throw UnsupportedSqlException(sql)
                }
                val gene = parts[0]
                val position = parts[1].toIntOrNull()
                val value = filterCondition.value
                if (position == null || value.length != 1) {
                    throw UnsupportedSqlException(sql)
                }
                aaMutations.add("${gene}:${position}${value}")
            } else {
                throw UnsupportedSqlException(sql)
            }

        }
        if (nucMutations.isNotEmpty()) {
            transformedQuery.filterConditions.add(
                FilterCondition("nucMutations", Operator.EqualsTo, nucMutations.joinToString(","))
            )
        }
        if (aaMutations.isNotEmpty()) {
            transformedQuery.filterConditions.add(
                FilterCondition("aaMutations", Operator.EqualsTo, aaMutations.joinToString(","))
            )
        }

        // groupByFields
        for (field in query.groupByFields) {
            transformedQuery.groupByFields.add(
                when (field) {
                    "region" -> "region"
                    "country" -> "country"
                    "date" -> "date"
                    else -> {
                        if (field.contains("lineage", true)) {
                            "nextcladePangoLineage"
                        } else {
                            throw UnsupportedSqlException(sql)
                        }
                    }
                }
            )
        }

        // orderByField
        val queryOrderByField = query.orderByField
        transformedQuery.orderByField = if (queryOrderByField != null) when (queryOrderByField) {
            "region" -> "region"
            "country" -> "country"
            "date" -> "date"
            "count" -> "count"
            else -> {
                if (queryOrderByField.contains("lineage", true)) {
                    "nextcladePangoLineage"
                } else if (queryOrderByField.contains("num") || queryOrderByField.contains("count")) {
                    "count"
                } else {
                    throw UnsupportedSqlException(sql)
                }
            }
        } else null

        // Other stuff
        transformedQuery.orderByAsc = query.orderByAsc
        transformedQuery.limit = query.limit

        return transformedQuery
    }

    private fun executeQuery(query: LapisSqlQuery): List<Map<String, String>> {
        if (!query.transformed) {
            throw Exception("Unexpected exception: validateAndTransformQuery() must be executed before this function")
        }

        /* Fetch data */
        var uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl("${host}/sample/aggregated")
        if (accessKey != null) {
            uriComponentsBuilder = uriComponentsBuilder.queryParam("accessKey", accessKey)
        }
        for (filterCondition in query.filterConditions) {
            uriComponentsBuilder = uriComponentsBuilder.queryParam(filterCondition.field, filterCondition.value)
        }
        if (query.groupByFields.isNotEmpty()) {
            uriComponentsBuilder = uriComponentsBuilder.queryParam("fields", query.groupByFields.joinToString(","))
        }
        val url = uriComponentsBuilder.encode().toUriString()
        val response = RestTemplate().getForEntity(url, LapisResponse::class.java)
        if (response.statusCode != HttpStatus.OK) {
            var urlWithoutHost = url.substring(host.length)
            if (accessKey != null) {
                urlWithoutHost = urlWithoutHost.replace(accessKey, "hidden")
            }
            throw LapisRequestFailedException(urlWithoutHost, response.statusCodeValue)
        }
        var data = response.body!!.data

        // Perform order by and limit if requested
        if (query.orderByField != null) {
            data = when (query.orderByField) {
                "count" -> data.sortedBy { it[query.orderByField]?.toInt() ?: Int.MAX_VALUE }
                else -> data.sortedBy { it[query.orderByField] ?: "ZZZZZZZZZZ" }
            }
            if (!query.orderByAsc) {
                data = data.reversed()
            }
        }
        if (query.limit != null) {
            data = data.subList(0, data.size.coerceAtMost(query.limit!!))
        }

        return data
    }
}

fun main() {
    val lapis = LapisSqlClient("https://lapis.cov-spectrum.org/open/v1", null)

    val sqls = listOf(
        """
            select date
            from lapis
            where country = 'Switzerland' and lineage = 'B.1.1.7'
            order by date asc
            limit 1;
        """.trimIndent(),
        """
            select date, count(*) as count
            from lapis
            where country = 'Switzerland'
            group by date
            order by date
            limit 1;
        """.trimIndent(),
        """
            select country, count(*) as count
            from lapis
            where lineage = 'BA.1'
            group by country
            order by count desc
            limit 1;
        """.trimIndent(),
        """
            select date, count(*) as count
            from lapis
            where country = 'Switzerland' and region = 'Asia';
        """.trimIndent(),
        """
            select date, count(*) as count
            from lapis
            where country = 'Switzerland' and region = 'Asia';
        """.trimIndent(),
        """
            select count(*)
            from lapis
            where country = 'Australia' and nuc_25563 = 'T' and aa_N_377 = 'Y';
        """.trimIndent()
    )
    for (sql in sqls) {
        val data = lapis.execute(sql)
        println("Next..")
    }
}
