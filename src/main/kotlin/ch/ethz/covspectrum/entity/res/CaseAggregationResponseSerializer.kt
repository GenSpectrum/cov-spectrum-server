package ch.ethz.covspectrum.entity.res

import ch.ethz.covspectrum.entity.req.CaseAggregationField
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import org.springframework.boot.jackson.JsonComponent


@JsonComponent
class CaseAggregationResponseSerializer : JsonSerializer<CaseAggregationResponse>() {
    override fun serialize(
        response: CaseAggregationResponse?,
        gen: JsonGenerator?,
        serializer: SerializerProvider?
    ) {
        check(response != null && gen != null)
        gen.writeStartArray()
        for (entry in response.entries) {
            gen.writeStartObject()
            for (field in response.fields) {
                when (field) {
                    CaseAggregationField.REGION -> gen.writeStringField("region", entry.region)
                    CaseAggregationField.COUNTRY -> gen.writeStringField("country", entry.country)
                    CaseAggregationField.DATE -> gen.writeObjectField("date", entry.date)
                }
            }
            gen.writeNumberField("newCases", entry.newCases ?: 0)
            gen.writeNumberField("newDeaths", entry.newDeaths ?: 0)
            gen.writeEndObject()
        }
        gen.writeEndArray()
    }
}
