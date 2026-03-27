package com.zgamelogic.data.intermediates.planData;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.StdSerializer;

import java.awt.*;

@JsonSerialize(using = DiscordRoleData.DiscordRoleDataSerializer.class)
public record DiscordRoleData(String roleName, Long id, Color color) {

    public static class DiscordRoleDataSerializer extends StdSerializer<DiscordRoleData> {
        protected DiscordRoleDataSerializer(){
            super(DiscordRoleData.class);
        }

        @Override
        public void serialize(DiscordRoleData value, tools.jackson.core.JsonGenerator gen, SerializationContext provider) throws JacksonException {
            gen.writeStartObject();
            gen.writeStringProperty("name", value.roleName);
            gen.writeNumberProperty("id", value.id);
            if(value.color != null) {
                gen.writeObjectPropertyStart("color");
                gen.writeNumberProperty("red", value.color.getRed());
                gen.writeNumberProperty("green", value.color.getGreen());
                gen.writeNumberProperty("blue", value.color.getBlue());
                gen.writeEndObject();
            }
            gen.writeEndObject();
        }
    }
}