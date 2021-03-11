package ch.ethz.covspectrum.jooq;

import org.jooq.*;
import org.jooq.impl.CustomField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

import java.time.LocalDate;


public class ExtractIsoYear extends CustomField<Integer> {

    private final Field<LocalDate> dateField;

    public ExtractIsoYear(Field<LocalDate> dateField) {
        super("extract_iso_year", SQLDataType.INTEGER);
        this.dateField = dateField;
    }

    @Override
    public void accept(Context<?> ctx) {
        ctx.visit(delegate(ctx.configuration()));
    }

    private QueryPart delegate(Configuration configuration) {
        return DSL.field("EXTRACT(ISOYEAR FROM {0})", String.class, dateField);
    }
}
