package ch.ethz.covspectrum.jooq;

import ch.ethz.covspectrum.entity.core.AAMutation;
import ch.ethz.covspectrum.entity.core.AAMutationDecoded;
import org.jooq.Record;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.threeten.extra.YearWeek;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class MyDSL {

    public static Field<Integer> extractIsoYear(Field<LocalDate> dateField) {
        return new ExtractIsoYear(dateField);
    }


    public static Table<?> groupByAndCount(DSLContext ctx, Table<?> table, List<Field<?>> fields,
                                           boolean allowNulls) {
        ArrayList<Field<?>> selectFields = new ArrayList<>(fields);
        selectFields.add(DSL.count().as("count"));
        SelectJoinStep<Record> statement0 = ctx
                .select(selectFields)
                .from(table);
        SelectConnectByStep<Record> statement1;
        if (allowNulls) {
            statement1 = statement0;
        }  else {
            Condition notNullCondition = fields.get(0).isNotNull();
            for (int i = 1; i < fields.size(); i++) {
                notNullCondition = notNullCondition.and(fields.get(i).isNotNull());
            }
            statement1 = statement0.where(notNullCondition);
        }
        SelectHavingStep<Record> statement2 = statement1.groupBy(fields);
        return statement2.asTable();
    }


    public static Condition yearWeekConstantEq(Table<?> table, YearWeek yearWeek) {
        return MyDSL.extractIsoYear(table.field("date", LocalDate.class)).eq(yearWeek.getYear())
                .and(DSL.extract(table.field("date"), DatePart.WEEK).eq(yearWeek.getWeek()));
    }


    public static Condition countryConstantEq(Table<?> table, String country) {
        return table.field("country", String.class).eq(country);
    }


    public static Condition aaMutationEq(Table<?> table, AAMutation aaMutation){
        AAMutationDecoded decoded = aaMutation.decode();
        Condition c = table.field("aa_mutation_gene", String.class).eq(decoded.getGene().toLowerCase())
                .and(table.field("aa_mutation_position", Integer.class).eq(decoded.getPosition()));
        if (decoded.getBase() != null) {
            c = c.and(table.field("aa_mutation_base", String.class).eq(decoded.getBase().toLowerCase()));
        }
        return c;
    }


    public static Table<?> metaJoinMut(Table<?> metadataTable, Table<?> mutationAaTable) {
        return metadataTable.join(mutationAaTable)
                .on(fSequenceName(metadataTable).eq(fSequenceName(mutationAaTable)));
    }


    public static Field<String> fSequenceName(Table<?> table) {
        return table.field("sequence_name", String.class);
    }


    public static Field<LocalDate> fDate(Table<?> table) {
        return table.field("date", LocalDate.class);
    }


    public static Field<String> fRegion(Table<?> table) {
        return table.field("region", String.class);
    }


    public static Field<String> fCountry(Table<?> table) {
        return table.field("country", String.class);
    }


    public static Field<String> fDivision(Table<?> table) {
        return table.field("division", String.class);
    }


    public static Field<String> fLocation(Table<?> table) {
        return table.field("location", String.class);
    }


    public static Field<String> fZipCode(Table<?> table) {
        return table.field("zip_code", String.class);
    }


    public static Field<String> fHost(Table<?> table) {
        return table.field("host", String.class);
    }


    public static Field<Integer> fAge(Table<?> table) {
        return table.field("age", Integer.class);
    }


    public static Field<String> fSex(Table<?> table) {
        return table.field("sex", String.class);
    }


    public static Field<String> fSubmittingLab(Table<?> table) {
        return table.field("submitting_lab", String.class);
    }


    public static Field<String> fOriginatingLab(Table<?> table) {
        return table.field("originating_lab", String.class);
    }


    public static Field<Boolean> fHospitalized(Table<?> table) {
        return table.field("hospitalized", Boolean.class);
    }


    public static Field<Boolean> fDeceased(Table<?> table) {
        return table.field("deceased", Boolean.class);
    }


    public static Field<String> fAaMutation(Table<?> table) {
        return table.field("aa_mutation", String.class);
    }


    public static Field<Integer> fCount(Table<?> table) {
        return table.field("count", Integer.class);
    }


    public static Field<String> fAgeGroup(Table<?> table) {
        return table.field("age_group", String.class);
    }

}
