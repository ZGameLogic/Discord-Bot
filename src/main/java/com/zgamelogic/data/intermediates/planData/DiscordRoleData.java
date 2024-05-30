package com.zgamelogic.data.intermediates.planData;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.awt.*;
import java.io.IOException;

@JsonSerialize(using = DiscordRoleData.DiscordRoleDataSerializer.class)
public record DiscordRoleData(String roleName, Long id, Color color) {

    public static class DiscordRoleDataSerializer extends JsonSerializer<DiscordRoleData> {
        @Override
        public void serialize(DiscordRoleData value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            gen.writeStringField("name", value.roleName);
            gen.writeNumberField("id", value.id);
            if(value.color != null) {
                gen.writeObjectFieldStart("color");
                gen.writeNumberField("red", value.color.getRed());
                gen.writeNumberField("green", value.color.getGreen());
                gen.writeNumberField("blue", value.color.getBlue());
                gen.writeEndObject();
            }
            gen.writeEndObject();
        }
    }
}
