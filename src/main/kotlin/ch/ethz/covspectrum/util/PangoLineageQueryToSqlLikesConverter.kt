package ch.ethz.covspectrum.util


class PangoLineageQueryToSqlLikesConverter(aliases: List<PangoLineageAlias>) {

    private val pangoLineageAliasResolver = PangoLineageAliasResolver(aliases)

    /**
     * This function translates a pangolin lineage query to an array of SQL like-statements. A sequence matches the
     * query if any like-statements are fulfilled. The like-statements are designed to be passed into the following SQL
     * statement: where pangolin_lineage like any(?)
     *
     *
     * Prefix search: Return the lineage and all sub-lineages. I.e., for both "B.1.*" and "B.1*", B.1 and all lineages
     * starting with "B.1." should be returned. "B.11" should not be returned.
     *
     *
     * Example: "B.1.2*" will return [B.1.2, B.1.2.%].
     */
    fun convert(query: String): List<String> {
        val finalQuery = query.uppercase()

        // Resolve aliases
        val resolvedQueries: MutableList<String> = mutableListOf(finalQuery)
        resolvedQueries.addAll(pangoLineageAliasResolver.findAlias(query))

        // Handle prefix search
        val result: MutableList<String> = ArrayList()
        for (resolvedQuery in resolvedQueries) {
            if (resolvedQuery.contains("%")) {
                // Nope, I don't want to allow undocumented features.
            } else if (!resolvedQuery.endsWith("*")) {
                result.add(resolvedQuery)
            } else {
                // Prefix search
                var rootLineage = resolvedQuery.substring(0, resolvedQuery.length - 1)
                if (rootLineage.endsWith(".")) {
                    rootLineage = rootLineage.substring(0, rootLineage.length - 1)
                }
                val subLineages = rootLineage + ".%"
                result.add(rootLineage)
                result.add(subLineages)
            }
        }
        return result
    }
}
