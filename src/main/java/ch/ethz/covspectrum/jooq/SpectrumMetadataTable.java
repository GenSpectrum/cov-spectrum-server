package ch.ethz.covspectrum.jooq;

import org.jooq.TableField;
import org.jooq.impl.CustomTable;
import org.jooq.impl.SQLDataType;

import java.time.LocalDate;

import static org.jooq.impl.DSL.name;


public class SpectrumMetadataTable extends CustomTable<SpectrumMetadataRecord> {

    public static final SpectrumMetadataTable PRIVATE
            = new SpectrumMetadataTable("spectrum_sequence_private_meta");

    public static final SpectrumMetadataTable PUBLIC
            = new SpectrumMetadataTable("spectrum_sequence_public_meta");

    public final TableField<SpectrumMetadataRecord, String> SEQUENCE_NAME
            = createField(name("sequence_name"), SQLDataType.CLOB);

    public final TableField<SpectrumMetadataRecord, LocalDate> DATE
            = createField(name("date"), SQLDataType.LOCALDATE);

    public final TableField<SpectrumMetadataRecord, String> REGION
            = createField(name("region"), SQLDataType.CLOB);

    public final TableField<SpectrumMetadataRecord, String> COUNTRY
            = createField(name("country"), SQLDataType.CLOB);

    public final TableField<SpectrumMetadataRecord, String> DIVISION
            = createField(name("division"), SQLDataType.CLOB);

    public final TableField<SpectrumMetadataRecord, String> LOCATION
            = createField(name("location"), SQLDataType.CLOB);

    public final TableField<SpectrumMetadataRecord, String> ZIP_CODE
            = createField(name("zip_code"), SQLDataType.CLOB);

    public final TableField<SpectrumMetadataRecord, String> HOST
            = createField(name("host"), SQLDataType.CLOB);

    public final TableField<SpectrumMetadataRecord, Integer> AGE
            = createField(name("age"), SQLDataType.INTEGER);

    public final TableField<SpectrumMetadataRecord, String> SEX
            = createField(name("sex"), SQLDataType.CLOB);

    public final TableField<SpectrumMetadataRecord, String> SUBMITTING_LAB
            = createField(name("submitting_lab"), SQLDataType.CLOB);

    public final TableField<SpectrumMetadataRecord, String> ORIGINATING_LAB
            = createField(name("originating_lab"), SQLDataType.CLOB);

    public final TableField<SpectrumMetadataRecord, Boolean> HOSPITALIZED
            = createField(name("hospitalized"), SQLDataType.BOOLEAN);

    public final TableField<SpectrumMetadataRecord, Boolean> DECEASED
            = createField(name("deceased"), SQLDataType.BOOLEAN);


    protected SpectrumMetadataTable(String tableName) {
        super(name(tableName));
    }

    @Override
    public Class<? extends SpectrumMetadataRecord> getRecordType() {
        return SpectrumMetadataRecord.class;
    }
}
