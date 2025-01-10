package com.zgamelogic.data.intermediates.dataotter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.io.IOException;
import java.util.List;

@Getter
@AllArgsConstructor
@JsonSerialize(using = SlashCommandRock.SlashCommandRockSerializer.class)
public class SlashCommandRock {
    private final String ROCK_TYPE = "slash command";
    private SlashCommandInteractionEvent event;

    public static class SlashCommandRockSerializer extends JsonSerializer<SlashCommandRock> {
        @Override
        public void serialize(SlashCommandRock value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            SlashCommandInteractionEvent event = value.event;
            gen.writeStartObject();
            gen.writeStringField("rock type", "slash command");
            gen.writeNumberField("user", event.getUser().getIdLong());
            gen.writeNumberField("id", event.getIdLong());
            gen.writeStringField("command name", event.getName());
            gen.writeStringField("sub command name", event.getSubcommandName());
            gen.writeNumberField("command id", event.getCommandIdLong());
            List<OptionMapping> optionList = event.getOptions();
            if(!optionList.isEmpty()) {
                gen.writeObjectFieldStart("options");
                for(OptionMapping option : optionList) {
                    gen.writeStringField(option.getName(), option.getAsString());
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
