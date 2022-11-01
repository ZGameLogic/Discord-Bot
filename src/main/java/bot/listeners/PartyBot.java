package bot.listeners;

import bot.utils.AdvancedListenerAdapter;
import data.ConfigLoader;
import data.database.guildData.GuildData;
import data.database.guildData.GuildDataRepository;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class PartyBot extends AdvancedListenerAdapter {

    GuildDataRepository guildData;

    public PartyBot(GuildDataRepository guildData){
        this.guildData = guildData;
//        addGuildCommands(config.getShlongshotId(), Commands.slash("rename-chatroom", "Renames chatroom to a new name")
//                .addOption(OptionType.STRING, "name", "Chatroom name", true),
//                Commands.slash("limit", "Limits the amount of people who can enter a chatroom")
//                        .addOption(OptionType.INTEGER, "count", "Number of people allowed in the chatroom", true)
//        );
    }

    @ButtonResponse(buttonId = "enable_party")
    private void enableParty(ButtonInteractionEvent event){
        event.editButton(Button.success("disable_party", "Party Bot")).queue();
        GuildData savedGuild = guildData.findById(event.getGuild().getIdLong()).get();
        savedGuild.setChatroomEnabled(true);
        guildData.save(savedGuild);
    }

    @ButtonResponse(buttonId = "disable_party")
    private void disableParty(ButtonInteractionEvent event){
        event.editButton(Button.danger("enable_party", "Party Bot")).queue();
        GuildData savedGuild = guildData.findById(event.getGuild().getIdLong()).get();
        savedGuild.setChatroomEnabled(false);
        guildData.save(savedGuild);
    }

    @SlashResponse(commandName = "rename-chatroom")
    private void renameChatroom(SlashCommandInteractionEvent event){

    }

    @SlashResponse(commandName = "limit")
    private void limit(SlashCommandInteractionEvent event){

    }
}
