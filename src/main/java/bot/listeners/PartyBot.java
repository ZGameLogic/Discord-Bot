package bot.listeners;

import bot.utils.AdvancedListenerAdapter;
import data.ConfigLoader;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class PartyBot extends AdvancedListenerAdapter {

    public PartyBot(ConfigLoader config){
        addGuildCommands(config.getShlongshotId(), Commands.slash("rename-chatroom", "Renames chatroom to a new name")
                .addOption(OptionType.STRING, "name", "Chatroom name", true),
                Commands.slash("limit", "Limits the amount of people who can enter a chatroom")
                        .addOption(OptionType.INTEGER, "count", "Number of people allowed in the chatroom", true)
        );
    }

    @SlashResponse(commandName = "rename-chatroom")
    private void renameChatroom(SlashCommandInteractionEvent event){

    }

    @SlashResponse(commandName = "limit")
    private void limit(SlashCommandInteractionEvent event){

    }
}
