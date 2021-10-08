package ch.ethz.covspectrum.entity.req

import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component


@Component
class CaseAggregationFieldListDeserializer: Converter<String, List<CaseAggregationField>> {
    override fun convert(value: String): List<CaseAggregationField>? {
        if (value.isBlank()) {
            return emptyList()
        }
        return value.split(",")
            .map { CaseAggregationField.valueOf(it.uppercase()) }
    }
}
