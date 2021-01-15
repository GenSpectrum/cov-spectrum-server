package ch.ethz.vm.entity;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;


@JsonComponent
public class AAMutationSerializer extends JsonSerializer<AAMutation> {
    @Override
    public void serialize(AAMutation value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(value.getMutationCode());
    }
}
