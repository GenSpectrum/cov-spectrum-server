package ch.ethz.covspectrum.chat

data class LapisSqlQuery(
    var selectFields: MutableList<String> = mutableListOf(),
    var filterConditions: MutableList<FilterCondition> = mutableListOf(),
    var groupByFields: MutableList<String> = mutableListOf(),
    var orderByField: String? = null,
    var orderByAsc: Boolean = true,
    var limit: Int? = null,
    var transformed: Boolean = false
) {
    data class FilterCondition(
        val field: String,
        val comparisonOperator: Operator,
        val value: String
    ) {
        enum class Operator {
            EqualsTo,
            GreaterThan,
            GreaterThanEqual,
            LessThan,
            LessThanEqual
        }
    }
}
