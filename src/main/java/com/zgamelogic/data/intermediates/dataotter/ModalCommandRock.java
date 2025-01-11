package com.zgamelogic.data.intermediates.dataotter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;

import java.io.IOException;
import java.util.List;

@Getter
@AllArgsConstructor
@JsonSerialize(using = ModalCommandRock.ModalRockSerializer.class)
public class ModalCommandRock {
    private ModalInteractionEvent event;

    public static class ModalRockSerializer extends JsonSerializer<ModalCommandRock> {
        @Override
        public void serialize(ModalCommandRock value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            ModalInteractionEvent event = value.event;
            gen.writeStartObject();
            gen.writeStringField("rock type", "modal response");
            gen.writeNumberField("user", event.getUser().getIdLong());
            gen.writeNumberField("id", event.getIdLong());
            List<ModalMapping> optionList = event.getValues();
            if(!optionList.isEmpty()) {
                gen.writeObjectFieldStart("options");
                for(ModalMapping option : optionList) {
                    gen.writeStringField(option.getId(), option.getAsString());
                }
                gen.writeEndObject();
            }
            if(event.isFromGuild()){
                gen.writeNumberField("guild", event.getGuild().getIdLong());
                gen.writeNumberField("channel", event.getChannel().getIdLong());
            }
            gen.writeEndObject();
        }
    }
}
