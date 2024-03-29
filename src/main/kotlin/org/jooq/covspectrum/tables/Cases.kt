/*
 * This file is generated by jOOQ.
 */
package org.jooq.covspectrum.tables


import java.time.LocalDate

import org.jooq.Field
import org.jooq.ForeignKey
import org.jooq.Name
import org.jooq.Record
import org.jooq.Row6
import org.jooq.Schema
import org.jooq.Table
import org.jooq.TableField
import org.jooq.TableOptions
import org.jooq.covspectrum.Public
import org.jooq.covspectrum.tables.records.CasesRecord
import org.jooq.impl.DSL
import org.jooq.impl.Internal
import org.jooq.impl.SQLDataType
import org.jooq.impl.TableImpl


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
open class Cases(
    alias: Name,
    child: Table<out Record>?,
    path: ForeignKey<out Record, CasesRecord>?,
    aliased: Table<CasesRecord>?,
    parameters: Array<Field<*>?>?
): TableImpl<CasesRecord>(
    alias,
    Public.PUBLIC,
    child,
    path,
    aliased,
    parameters,
    DSL.comment(""),
    TableOptions.view("create view \"cases\" as  SELECT cm.cov_spectrum_region AS region,\n    cm.cov_spectrum_country AS country,\n    NULL::text AS division,\n    cro.date,\n    COALESCE(cro.new_cases, 0) AS new_cases,\n    COALESCE(cro.new_deaths, 0) AS new_deaths\n   FROM (cases_raw_owid cro\n     JOIN country_mapping cm ON ((cro.country = cm.owid_country)));")
) {
    companion object {

        /**
         * The reference instance of <code>public.cases</code>
         */
        val CASES: Cases = Cases()
    }

    /**
     * The class holding records for this type
     */
    override fun getRecordType(): Class<CasesRecord> = CasesRecord::class.java

    /**
     * The column <code>public.cases.region</code>.
     */
    val REGION: TableField<CasesRecord, String?> = createField(DSL.name("region"), SQLDataType.CLOB, this, "")

    /**
     * The column <code>public.cases.country</code>.
     */
    val COUNTRY: TableField<CasesRecord, String?> = createField(DSL.name("country"), SQLDataType.CLOB, this, "")

    /**
     * The column <code>public.cases.division</code>.
     */
    val DIVISION: TableField<CasesRecord, String?> = createField(DSL.name("division"), SQLDataType.CLOB, this, "")

    /**
     * The column <code>public.cases.date</code>.
     */
    val DATE: TableField<CasesRecord, LocalDate?> = createField(DSL.name("date"), SQLDataType.LOCALDATE, this, "")

    /**
     * The column <code>public.cases.new_cases</code>.
     */
    val NEW_CASES: TableField<CasesRecord, Int?> = createField(DSL.name("new_cases"), SQLDataType.INTEGER, this, "")

    /**
     * The column <code>public.cases.new_deaths</code>.
     */
    val NEW_DEATHS: TableField<CasesRecord, Int?> = createField(DSL.name("new_deaths"), SQLDataType.INTEGER, this, "")

    private constructor(alias: Name, aliased: Table<CasesRecord>?): this(alias, null, null, aliased, null)
    private constructor(alias: Name, aliased: Table<CasesRecord>?, parameters: Array<Field<*>?>?): this(alias, null, null, aliased, parameters)

    /**
     * Create an aliased <code>public.cases</code> table reference
     */
    constructor(alias: String): this(DSL.name(alias))

    /**
     * Create an aliased <code>public.cases</code> table reference
     */
    constructor(alias: Name): this(alias, null)

    /**
     * Create a <code>public.cases</code> table reference
     */
    constructor(): this(DSL.name("cases"), null)

    constructor(child: Table<out Record>, key: ForeignKey<out Record, CasesRecord>): this(Internal.createPathAlias(child, key), child, key, CASES, null)
    override fun getSchema(): Schema? = if (aliased()) null else Public.PUBLIC
    override fun `as`(alias: String): Cases = Cases(DSL.name(alias), this)
    override fun `as`(alias: Name): Cases = Cases(alias, this)

    /**
     * Rename this table
     */
    override fun rename(name: String): Cases = Cases(DSL.name(name), null)

    /**
     * Rename this table
     */
    override fun rename(name: Name): Cases = Cases(name, null)

    // -------------------------------------------------------------------------
    // Row6 type methods
    // -------------------------------------------------------------------------
    override fun fieldsRow(): Row6<String?, String?, String?, LocalDate?, Int?, Int?> = super.fieldsRow() as Row6<String?, String?, String?, LocalDate?, Int?, Int?>
}
