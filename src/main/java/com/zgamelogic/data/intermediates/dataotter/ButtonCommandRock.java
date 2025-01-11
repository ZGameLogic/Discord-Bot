package com.zgamelogic.data.intermediates.dataotter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.io.IOException;

@Getter
@AllArgsConstructor
@JsonSerialize(using = ButtonCommandRock.ButtonCommandRockSerializer.class)
public class ButtonCommandRock {
    private final String ROCK_TYPE = "button command";
    private ButtonInteractionEvent event;

    public static class ButtonCommandRockSerializer extends JsonSerializer<ButtonCommandRock> {
        @Override
        public void serialize(ButtonCommandRock value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            ButtonInteractionEvent event = value.event;
            gen.writeStartObject();
            gen.writeStringField("rock type", "button interaction");
            gen.writeNumberField("user", event.getUser().getIdLong());
            gen.writeNumberField("id", event.getIdLong());
            gen.writeStringField("button id", event.getButton().getId());
            if(event.isFromGuild()){
                gen.writeNumberField("guild", event.getGuild().getIdLong());
                gen.writeNumberField("channel", event.getChannel().getIdLong());
            }
            gen.writeEndObject();
        }
    }
}
