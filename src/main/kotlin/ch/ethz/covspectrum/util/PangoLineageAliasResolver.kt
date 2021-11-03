package ch.ethz.covspectrum.util

class PangoLineageAliasResolver(private val aliases: List<PangoLineageAlias>) {
    /**
     * Examples:
     * ```
     * - B.1.1.1.2 -> [C.2]
     * - B.1.1.1   -> [C]
     * - B.1.1.1*  -> [C.*]
     * - B.1.1*    -> [C.*, D.*, K.*, ...]
     * - B.1.1.12* -> []
     * ```
     */
    fun findAlias(query: String): List<String> {
        // We need to consider the following cases:
        //   Prefix search:
        //     - yes
        //     - no
        //   Query length vs. alias' full name length (The full names of the aliases always have 3 number components):
        //     - Query has less than 3 number components
        //     - Query has at least 3 number components
        //
        // No prefix search + same length/query is longer: 0 or 1 match   -> alias' full name is a prefix of the query
        // No prefix search + query is shorter: 0 match
        // Prefix search    + same length/query is longer: 0 or 1 match   -> alias' full name is a prefix of the query
        // Prefix search    + query is shorter: multiple matches possible -> query is a prefix of the alias' full name
        val prefixSearch = query.endsWith("*")
        var queryRoot = query // The query without a tailing *
        if (prefixSearch) {
            queryRoot = queryRoot.substring(0, queryRoot.length - 1)
            if (queryRoot.endsWith(".")) {
                queryRoot = queryRoot.substring(0, queryRoot.length - 1)
            }
        }
        val finalQueryRoot = queryRoot
        val queryIsShorter = queryRoot.toCharArray().count { it == '.' } < 3
        if (!queryIsShorter) {
            return aliases
                .filter { finalQueryRoot == it.fullName || finalQueryRoot.startsWith(it.fullName + ".") }
                .map { it.alias + finalQueryRoot.substring(it.fullName.length) + (if (prefixSearch) ".*" else "") }
        }
        if (!prefixSearch && queryIsShorter) {
            return emptyList()
        }
        if (prefixSearch && queryIsShorter) {
            return aliases
                .filter { it.fullName == finalQueryRoot || it.fullName.startsWith(finalQueryRoot + ".") }
                .map { it.alias + ".*" }
        }
        throw RuntimeException("Unexpected error: This should be unreachable. Query: $query")
    }
}
