package com.zgamelogic.bot.listeners;

import com.zgamelogic.bot.utils.dungeon.data.Dungeon;
import com.zgamelogic.discord.annotations.DiscordController;
import com.zgamelogic.discord.annotations.DiscordMapping;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.utils.FileUpload;
import org.springframework.context.annotation.Bean;

import java.io.File;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.zgamelogic.bot.utils.dungeon.DungeonGenerator.*;

@DiscordController
public class GeneratorBot {
    @Bean
    private CommandData generatorCommand(){
        return Commands.slash("generate", "Generator commands")
            .addSubcommands(
                    new SubcommandData("dungeon", "Dungeon generator")
                            .addOption(OptionType.STRING, "size", "Size of the dungeon", false, true)
            );
    }

    @DiscordMapping(Id = "generate", SubId = "dungeon", FocusedOption = "size")
    private void dungeonSizeAutoCompleteResponse(CommandAutoCompleteInteractionEvent event){
        event.replyChoices(
                Stream.of(new String[]{"small", "medium", "large"})
                .map(word -> new Command.Choice(word, word))
                .collect(Collectors.toList())
        ).queue();
    }

    @DiscordMapping(Id = "generate", SubId = "dungeon")
    private void generateDungeonSlashCommand(SlashCommandInteractionEvent event){
        event.deferReply().queue();
        Dungeon dungeon;
        if(event.getOption("size") == null) {
            dungeon = GenerateRandomDungeon();
        } else {
            dungeon = switch (event.getOption("size").getAsString()) {
                case "small" -> GenerateDungeon(Size.SMALL);
                case "medium" -> GenerateDungeon(Size.MEDIUM);
                case "large" -> GenerateDungeon(Size.LARGE);
                default -> GenerateRandomDungeon();
            };
        }
        File file = saveDungeon(dungeon);
        event.getHook().sendFiles(FileUpload.fromData(file)).complete();
        file.delete();
    }
}
