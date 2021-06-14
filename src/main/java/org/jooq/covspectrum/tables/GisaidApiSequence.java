/*
 * This file is generated by jOOQ.
 */
package org.jooq.covspectrum.tables;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.covspectrum.Keys;
import org.jooq.covspectrum.Public;
import org.jooq.covspectrum.tables.records.GisaidApiSequenceRecord;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class GisaidApiSequence extends TableImpl<GisaidApiSequenceRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.gisaid_api_sequence</code>
     */
    public static final GisaidApiSequence GISAID_API_SEQUENCE = new GisaidApiSequence();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<GisaidApiSequenceRecord> getRecordType() {
        return GisaidApiSequenceRecord.class;
    }

    /**
     * The column <code>public.gisaid_api_sequence.updated_at</code>.
     */
    public final TableField<GisaidApiSequenceRecord, LocalDateTime> UPDATED_AT = createField(DSL.name("updated_at"), SQLDataType.LOCALDATETIME(6).nullable(false), this, "");

    /**
     * The column <code>public.gisaid_api_sequence.gisaid_epi_isl</code>.
     */
    public final TableField<GisaidApiSequenceRecord, String> GISAID_EPI_ISL = createField(DSL.name("gisaid_epi_isl"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.gisaid_api_sequence.strain</code>.
     */
    public final TableField<GisaidApiSequenceRecord, String> STRAIN = createField(DSL.name("strain"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.gisaid_api_sequence.virus</code>.
     */
    public final TableField<GisaidApiSequenceRecord, String> VIRUS = createField(DSL.name("virus"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.gisaid_api_sequence.date</code>.
     */
    public final TableField<GisaidApiSequenceRecord, LocalDate> DATE = createField(DSL.name("date"), SQLDataType.LOCALDATE, this, "");

    /**
     * The column <code>public.gisaid_api_sequence.date_original</code>.
     */
    public final TableField<GisaidApiSequenceRecord, String> DATE_ORIGINAL = createField(DSL.name("date_original"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.gisaid_api_sequence.country</code>.
     */
    public final TableField<GisaidApiSequenceRecord, String> COUNTRY = createField(DSL.name("country"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.gisaid_api_sequence.region_original</code>.
     */
    public final TableField<GisaidApiSequenceRecord, String> REGION_ORIGINAL = createField(DSL.name("region_original"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.gisaid_api_sequence.country_original</code>.
     */
    public final TableField<GisaidApiSequenceRecord, String> COUNTRY_ORIGINAL = createField(DSL.name("country_original"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.gisaid_api_sequence.division</code>.
     */
    public final TableField<GisaidApiSequenceRecord, String> DIVISION = createField(DSL.name("division"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.gisaid_api_sequence.location</code>.
     */
    public final TableField<GisaidApiSequenceRecord, String> LOCATION = createField(DSL.name("location"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.gisaid_api_sequence.host</code>.
     */
    public final TableField<GisaidApiSequenceRecord, String> HOST = createField(DSL.name("host"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.gisaid_api_sequence.age</code>.
     */
    public final TableField<GisaidApiSequenceRecord, Integer> AGE = createField(DSL.name("age"), SQLDataType.INTEGER, this, "");

    /**
     * The column <code>public.gisaid_api_sequence.sex</code>.
     */
    public final TableField<GisaidApiSequenceRecord, String> SEX = createField(DSL.name("sex"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.gisaid_api_sequence.pangolin_lineage</code>.
     */
    public final TableField<GisaidApiSequenceRecord, String> PANGOLIN_LINEAGE = createField(DSL.name("pangolin_lineage"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.gisaid_api_sequence.gisaid_clade</code>.
     */
    public final TableField<GisaidApiSequenceRecord, String> GISAID_CLADE = createField(DSL.name("gisaid_clade"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.gisaid_api_sequence.originating_lab</code>.
     */
    public final TableField<GisaidApiSequenceRecord, String> ORIGINATING_LAB = createField(DSL.name("originating_lab"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.gisaid_api_sequence.submitting_lab</code>.
     */
    public final TableField<GisaidApiSequenceRecord, String> SUBMITTING_LAB = createField(DSL.name("submitting_lab"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.gisaid_api_sequence.date_submitted</code>.
     */
    public final TableField<GisaidApiSequenceRecord, LocalDate> DATE_SUBMITTED = createField(DSL.name("date_submitted"), SQLDataType.LOCALDATE, this, "");

    /**
     * The column <code>public.gisaid_api_sequence.sampling_strategy</code>.
     */
    public final TableField<GisaidApiSequenceRecord, String> SAMPLING_STRATEGY = createField(DSL.name("sampling_strategy"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.gisaid_api_sequence.seq_original</code>.
     */
    public final TableField<GisaidApiSequenceRecord, String> SEQ_ORIGINAL = createField(DSL.name("seq_original"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.gisaid_api_sequence.seq_aligned</code>.
     */
    public final TableField<GisaidApiSequenceRecord, String> SEQ_ALIGNED = createField(DSL.name("seq_aligned"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.gisaid_api_sequence.nextclade_clade</code>.
     */
    public final TableField<GisaidApiSequenceRecord, String> NEXTCLADE_CLADE = createField(DSL.name("nextclade_clade"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.gisaid_api_sequence.nextclade_qc_overall_score</code>.
     */
    public final TableField<GisaidApiSequenceRecord, Double> NEXTCLADE_QC_OVERALL_SCORE = createField(DSL.name("nextclade_qc_overall_score"), SQLDataType.DOUBLE, this, "");

    /**
     * The column <code>public.gisaid_api_sequence.nextclade_qc_overall_status</code>.
     */
    public final TableField<GisaidApiSequenceRecord, String> NEXTCLADE_QC_OVERALL_STATUS = createField(DSL.name("nextclade_qc_overall_status"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.gisaid_api_sequence.nextclade_total_gaps</code>.
     */
    public final TableField<GisaidApiSequenceRecord, Integer> NEXTCLADE_TOTAL_GAPS = createField(DSL.name("nextclade_total_gaps"), SQLDataType.INTEGER, this, "");

    /**
     * The column <code>public.gisaid_api_sequence.nextclade_total_insertions</code>.
     */
    public final TableField<GisaidApiSequenceRecord, Integer> NEXTCLADE_TOTAL_INSERTIONS = createField(DSL.name("nextclade_total_insertions"), SQLDataType.INTEGER, this, "");

    /**
     * The column <code>public.gisaid_api_sequence.nextclade_total_missing</code>.
     */
    public final TableField<GisaidApiSequenceRecord, Integer> NEXTCLADE_TOTAL_MISSING = createField(DSL.name("nextclade_total_missing"), SQLDataType.INTEGER, this, "");

    /**
     * The column <code>public.gisaid_api_sequence.nextclade_total_mutations</code>.
     */
    public final TableField<GisaidApiSequenceRecord, Integer> NEXTCLADE_TOTAL_MUTATIONS = createField(DSL.name("nextclade_total_mutations"), SQLDataType.INTEGER, this, "");

    /**
     * The column <code>public.gisaid_api_sequence.nextclade_total_non_acgtns</code>.
     */
    public final TableField<GisaidApiSequenceRecord, Integer> NEXTCLADE_TOTAL_NON_ACGTNS = createField(DSL.name("nextclade_total_non_acgtns"), SQLDataType.INTEGER, this, "");

    /**
     * The column <code>public.gisaid_api_sequence.nextclade_total_pcr_primer_changes</code>.
     */
    public final TableField<GisaidApiSequenceRecord, Integer> NEXTCLADE_TOTAL_PCR_PRIMER_CHANGES = createField(DSL.name("nextclade_total_pcr_primer_changes"), SQLDataType.INTEGER, this, "");

    /**
     * The column <code>public.gisaid_api_sequence.nextclade_alignment_start</code>.
     */
    public final TableField<GisaidApiSequenceRecord, Integer> NEXTCLADE_ALIGNMENT_START = createField(DSL.name("nextclade_alignment_start"), SQLDataType.INTEGER, this, "");

    /**
     * The column <code>public.gisaid_api_sequence.nextclade_alignment_end</code>.
     */
    public final TableField<GisaidApiSequenceRecord, Integer> NEXTCLADE_ALIGNMENT_END = createField(DSL.name("nextclade_alignment_end"), SQLDataType.INTEGER, this, "");

    /**
     * The column <code>public.gisaid_api_sequence.nextclade_alignment_score</code>.
     */
    public final TableField<GisaidApiSequenceRecord, Integer> NEXTCLADE_ALIGNMENT_SCORE = createField(DSL.name("nextclade_alignment_score"), SQLDataType.INTEGER, this, "");

    /**
     * The column <code>public.gisaid_api_sequence.nextclade_qc_missing_data_score</code>.
     */
    public final TableField<GisaidApiSequenceRecord, Double> NEXTCLADE_QC_MISSING_DATA_SCORE = createField(DSL.name("nextclade_qc_missing_data_score"), SQLDataType.DOUBLE, this, "");

    /**
     * The column <code>public.gisaid_api_sequence.nextclade_qc_missing_data_status</code>.
     */
    public final TableField<GisaidApiSequenceRecord, String> NEXTCLADE_QC_MISSING_DATA_STATUS = createField(DSL.name("nextclade_qc_missing_data_status"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.gisaid_api_sequence.nextclade_qc_missing_data_total</code>.
     */
    public final TableField<GisaidApiSequenceRecord, Integer> NEXTCLADE_QC_MISSING_DATA_TOTAL = createField(DSL.name("nextclade_qc_missing_data_total"), SQLDataType.INTEGER, this, "");

    /**
     * The column <code>public.gisaid_api_sequence.nextclade_qc_mixed_sites_score</code>.
     */
    public final TableField<GisaidApiSequenceRecord, Double> NEXTCLADE_QC_MIXED_SITES_SCORE = createField(DSL.name("nextclade_qc_mixed_sites_score"), SQLDataType.DOUBLE, this, "");

    /**
     * The column <code>public.gisaid_api_sequence.nextclade_qc_mixed_sites_status</code>.
     */
    public final TableField<GisaidApiSequenceRecord, String> NEXTCLADE_QC_MIXED_SITES_STATUS = createField(DSL.name("nextclade_qc_mixed_sites_status"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.gisaid_api_sequence.nextclade_qc_mixed_sites_total</code>.
     */
    public final TableField<GisaidApiSequenceRecord, Integer> NEXTCLADE_QC_MIXED_SITES_TOTAL = createField(DSL.name("nextclade_qc_mixed_sites_total"), SQLDataType.INTEGER, this, "");

    /**
     * The column <code>public.gisaid_api_sequence.nextclade_qc_private_mutations_cutoff</code>.
     */
    public final TableField<GisaidApiSequenceRecord, Integer> NEXTCLADE_QC_PRIVATE_MUTATIONS_CUTOFF = createField(DSL.name("nextclade_qc_private_mutations_cutoff"), SQLDataType.INTEGER, this, "");

    /**
     * The column <code>public.gisaid_api_sequence.nextclade_qc_private_mutations_excess</code>.
     */
    public final TableField<GisaidApiSequenceRecord, Integer> NEXTCLADE_QC_PRIVATE_MUTATIONS_EXCESS = createField(DSL.name("nextclade_qc_private_mutations_excess"), SQLDataType.INTEGER, this, "");

    /**
     * The column <code>public.gisaid_api_sequence.nextclade_qc_private_mutations_score</code>.
     */
    public final TableField<GisaidApiSequenceRecord, Double> NEXTCLADE_QC_PRIVATE_MUTATIONS_SCORE = createField(DSL.name("nextclade_qc_private_mutations_score"), SQLDataType.DOUBLE, this, "");

    /**
     * The column <code>public.gisaid_api_sequence.nextclade_qc_private_mutations_status</code>.
     */
    public final TableField<GisaidApiSequenceRecord, String> NEXTCLADE_QC_PRIVATE_MUTATIONS_STATUS = createField(DSL.name("nextclade_qc_private_mutations_status"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.gisaid_api_sequence.nextclade_qc_private_mutations_total</code>.
     */
    public final TableField<GisaidApiSequenceRecord, Integer> NEXTCLADE_QC_PRIVATE_MUTATIONS_TOTAL = createField(DSL.name("nextclade_qc_private_mutations_total"), SQLDataType.INTEGER, this, "");

    /**
     * The column <code>public.gisaid_api_sequence.nextclade_qc_snp_clusters_clustered</code>.
     */
    public final TableField<GisaidApiSequenceRecord, String> NEXTCLADE_QC_SNP_CLUSTERS_CLUSTERED = createField(DSL.name("nextclade_qc_snp_clusters_clustered"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.gisaid_api_sequence.nextclade_qc_snp_clusters_score</code>.
     */
    public final TableField<GisaidApiSequenceRecord, Double> NEXTCLADE_QC_SNP_CLUSTERS_SCORE = createField(DSL.name("nextclade_qc_snp_clusters_score"), SQLDataType.DOUBLE, this, "");

    /**
     * The column <code>public.gisaid_api_sequence.nextclade_qc_snp_clusters_status</code>.
     */
    public final TableField<GisaidApiSequenceRecord, String> NEXTCLADE_QC_SNP_CLUSTERS_STATUS = createField(DSL.name("nextclade_qc_snp_clusters_status"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.gisaid_api_sequence.nextclade_qc_snp_clusters_total</code>.
     */
    public final TableField<GisaidApiSequenceRecord, Integer> NEXTCLADE_QC_SNP_CLUSTERS_TOTAL = createField(DSL.name("nextclade_qc_snp_clusters_total"), SQLDataType.INTEGER, this, "");

    /**
     * The column <code>public.gisaid_api_sequence.nextclade_errors</code>.
     */
    public final TableField<GisaidApiSequenceRecord, String> NEXTCLADE_ERRORS = createField(DSL.name("nextclade_errors"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.gisaid_api_sequence.authors</code>.
     */
    public final TableField<GisaidApiSequenceRecord, String> AUTHORS = createField(DSL.name("authors"), SQLDataType.CLOB, this, "");

    private GisaidApiSequence(Name alias, Table<GisaidApiSequenceRecord> aliased) {
        this(alias, aliased, null);
    }

    private GisaidApiSequence(Name alias, Table<GisaidApiSequenceRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.gisaid_api_sequence</code> table reference
     */
    public GisaidApiSequence(String alias) {
        this(DSL.name(alias), GISAID_API_SEQUENCE);
    }

    /**
     * Create an aliased <code>public.gisaid_api_sequence</code> table reference
     */
    public GisaidApiSequence(Name alias) {
        this(alias, GISAID_API_SEQUENCE);
    }

    /**
     * Create a <code>public.gisaid_api_sequence</code> table reference
     */
    public GisaidApiSequence() {
        this(DSL.name("gisaid_api_sequence"), null);
    }

    public <O extends Record> GisaidApiSequence(Table<O> child, ForeignKey<O, GisaidApiSequenceRecord> key) {
        super(child, key, GISAID_API_SEQUENCE);
    }

    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    @Override
    public UniqueKey<GisaidApiSequenceRecord> getPrimaryKey() {
        return Keys.GISAID_API_SEQUENCE_PKEY;
    }

    @Override
    public List<UniqueKey<GisaidApiSequenceRecord>> getKeys() {
        return Arrays.<UniqueKey<GisaidApiSequenceRecord>>asList(Keys.GISAID_API_SEQUENCE_PKEY);
    }

    @Override
    public GisaidApiSequence as(String alias) {
        return new GisaidApiSequence(DSL.name(alias), this);
    }

    @Override
    public GisaidApiSequence as(Name alias) {
        return new GisaidApiSequence(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public GisaidApiSequence rename(String name) {
        return new GisaidApiSequence(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public GisaidApiSequence rename(Name name) {
        return new GisaidApiSequence(name, null);
    }
}