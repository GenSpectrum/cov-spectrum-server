package ch.ethz.vm.entity;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.jackson.JsonComponent;
import org.threeten.extra.YearWeek;

import java.io.IOException;


@JsonComponent
public class YearWeekSerializer extends JsonSerializer<YearWeek> {

    @Override
    public void serialize(YearWeek value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(value.getYear() + "-" + value.getWeek());
    }
}
