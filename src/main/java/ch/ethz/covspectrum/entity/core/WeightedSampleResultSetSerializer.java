package ch.ethz.covspectrum.entity.core;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;


@JsonComponent
public class WeightedSampleResultSetSerializer extends JsonSerializer<WeightedSampleResultSet> {
    @Override
    public void serialize(WeightedSampleResultSet value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        gen.writeStartArray();
        for (WeightedSample weightedSample : value.getWeightedSamples()) {
            gen.writeStartObject();
            for (String field : value.getFields()) {
                switch (field) {
                    case "date" -> gen.writeObjectField("date", weightedSample.getDate());
                    case "region" -> gen.writeStringField("region", weightedSample.getRegion());
                    case "country" -> gen.writeStringField("country", weightedSample.getCountry());
                    case "division" -> gen.writeStringField("division", weightedSample.getDivision());
                    case "zipCode" -> gen.writeStringField("zipCode", weightedSample.getZipCode());
                    case "ageGroup" -> gen.writeStringField("ageGroup", weightedSample.getAgeGroup());
                    case "sex" -> gen.writeStringField("sex", weightedSample.getSex());
                    case "hospitalized" -> gen.writeObjectField("hospitalized",
                            weightedSample.getHospitalized());
                    case "deceased" -> gen.writeObjectField("deceased", weightedSample.getDeceased());
                    case "pangolinLineage" -> gen.writeObjectField("pangolinLineage", weightedSample.getPangolinLineage());
                    case "submittingLab" -> gen.writeObjectField("submittingLab", weightedSample.getSubmittingLab());
                    default -> throw new RuntimeException("Unexpected field name: " + field);
                }
            }
            gen.writeNumberField("count", weightedSample.getCount());
            gen.writeEndObject();
        }
        gen.writeEndArray();
    }
}
