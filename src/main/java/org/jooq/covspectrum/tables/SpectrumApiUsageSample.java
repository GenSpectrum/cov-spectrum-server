/*
 * This file is generated by jOOQ.
 */
package org.jooq.covspectrum.tables;


import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row14;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.covspectrum.Keys;
import org.jooq.covspectrum.Public;
import org.jooq.covspectrum.tables.records.SpectrumApiUsageSampleRecord;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class SpectrumApiUsageSample extends TableImpl<SpectrumApiUsageSampleRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.spectrum_api_usage_sample</code>
     */
    public static final SpectrumApiUsageSample SPECTRUM_API_USAGE_SAMPLE = new SpectrumApiUsageSample();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<SpectrumApiUsageSampleRecord> getRecordType() {
        return SpectrumApiUsageSampleRecord.class;
    }

    /**
     * The column <code>public.spectrum_api_usage_sample.id</code>.
     */
    public final TableField<SpectrumApiUsageSampleRecord, Integer> ID = createField(DSL.name("id"), SQLDataType.INTEGER.nullable(false).identity(true), this, "");

    /**
     * The column <code>public.spectrum_api_usage_sample.isoyear</code>.
     */
    public final TableField<SpectrumApiUsageSampleRecord, Integer> ISOYEAR = createField(DSL.name("isoyear"), SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>public.spectrum_api_usage_sample.isoweek</code>.
     */
    public final TableField<SpectrumApiUsageSampleRecord, Integer> ISOWEEK = createField(DSL.name("isoweek"), SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>public.spectrum_api_usage_sample.usage_count</code>.
     */
    public final TableField<SpectrumApiUsageSampleRecord, Integer> USAGE_COUNT = createField(DSL.name("usage_count"), SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>public.spectrum_api_usage_sample.fields</code>.
     */
    public final TableField<SpectrumApiUsageSampleRecord, String> FIELDS = createField(DSL.name("fields"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.spectrum_api_usage_sample.private_version</code>.
     */
    public final TableField<SpectrumApiUsageSampleRecord, Boolean> PRIVATE_VERSION = createField(DSL.name("private_version"), SQLDataType.BOOLEAN.nullable(false), this, "");

    /**
     * The column <code>public.spectrum_api_usage_sample.region</code>.
     */
    public final TableField<SpectrumApiUsageSampleRecord, String> REGION = createField(DSL.name("region"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.spectrum_api_usage_sample.country</code>.
     */
    public final TableField<SpectrumApiUsageSampleRecord, String> COUNTRY = createField(DSL.name("country"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.spectrum_api_usage_sample.mutations</code>.
     */
    public final TableField<SpectrumApiUsageSampleRecord, String> MUTATIONS = createField(DSL.name("mutations"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.spectrum_api_usage_sample.match_percentage</code>.
     */
    public final TableField<SpectrumApiUsageSampleRecord, Double> MATCH_PERCENTAGE = createField(DSL.name("match_percentage"), SQLDataType.DOUBLE.nullable(false), this, "");

    /**
     * The column <code>public.spectrum_api_usage_sample.data_type</code>.
     */
    public final TableField<SpectrumApiUsageSampleRecord, String> DATA_TYPE = createField(DSL.name("data_type"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.spectrum_api_usage_sample.date_from</code>.
     */
    public final TableField<SpectrumApiUsageSampleRecord, LocalDate> DATE_FROM = createField(DSL.name("date_from"), SQLDataType.LOCALDATE.nullable(false), this, "");

    /**
     * The column <code>public.spectrum_api_usage_sample.date_to</code>.
     */
    public final TableField<SpectrumApiUsageSampleRecord, LocalDate> DATE_TO = createField(DSL.name("date_to"), SQLDataType.LOCALDATE.nullable(false), this, "");

    /**
     * The column <code>public.spectrum_api_usage_sample.pangolin_lineage</code>.
     */
    public final TableField<SpectrumApiUsageSampleRecord, String> PANGOLIN_LINEAGE = createField(DSL.name("pangolin_lineage"), SQLDataType.CLOB, this, "");

    private SpectrumApiUsageSample(Name alias, Table<SpectrumApiUsageSampleRecord> aliased) {
        this(alias, aliased, null);
    }

    private SpectrumApiUsageSample(Name alias, Table<SpectrumApiUsageSampleRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.spectrum_api_usage_sample</code> table reference
     */
    public SpectrumApiUsageSample(String alias) {
        this(DSL.name(alias), SPECTRUM_API_USAGE_SAMPLE);
    }

    /**
     * Create an aliased <code>public.spectrum_api_usage_sample</code> table reference
     */
    public SpectrumApiUsageSample(Name alias) {
        this(alias, SPECTRUM_API_USAGE_SAMPLE);
    }

    /**
     * Create a <code>public.spectrum_api_usage_sample</code> table reference
     */
    public SpectrumApiUsageSample() {
        this(DSL.name("spectrum_api_usage_sample"), null);
    }

    public <O extends Record> SpectrumApiUsageSample(Table<O> child, ForeignKey<O, SpectrumApiUsageSampleRecord> key) {
        super(child, key, SPECTRUM_API_USAGE_SAMPLE);
    }

    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    @Override
    public Identity<SpectrumApiUsageSampleRecord, Integer> getIdentity() {
        return (Identity<SpectrumApiUsageSampleRecord, Integer>) super.getIdentity();
    }

    @Override
    public UniqueKey<SpectrumApiUsageSampleRecord> getPrimaryKey() {
        return Keys.SPECTRUM_API_USAGE_SAMPLE_PKEY;
    }

    @Override
    public List<UniqueKey<SpectrumApiUsageSampleRecord>> getKeys() {
        return Arrays.<UniqueKey<SpectrumApiUsageSampleRecord>>asList(Keys.SPECTRUM_API_USAGE_SAMPLE_PKEY, Keys.SPECTRUM_API_USAGE_SAMPLE_UNIQUE_CONSTRAINT);
    }

    @Override
    public SpectrumApiUsageSample as(String alias) {
        return new SpectrumApiUsageSample(DSL.name(alias), this);
    }

    @Override
    public SpectrumApiUsageSample as(Name alias) {
        return new SpectrumApiUsageSample(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public SpectrumApiUsageSample rename(String name) {
        return new SpectrumApiUsageSample(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public SpectrumApiUsageSample rename(Name name) {
        return new SpectrumApiUsageSample(name, null);
    }

    // -------------------------------------------------------------------------
    // Row14 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row14<Integer, Integer, Integer, Integer, String, Boolean, String, String, String, Double, String, LocalDate, LocalDate, String> fieldsRow() {
        return (Row14) super.fieldsRow();
    }
}