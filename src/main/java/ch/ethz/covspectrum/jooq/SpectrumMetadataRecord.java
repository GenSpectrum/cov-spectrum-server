package ch.ethz.covspectrum.jooq;

import org.jooq.Table;
import org.jooq.impl.CustomRecord;


public class SpectrumMetadataRecord extends CustomRecord<SpectrumMetadataRecord> {

    protected SpectrumMetadataRecord(Table<SpectrumMetadataRecord> table) {
        super(table);
    }

}
