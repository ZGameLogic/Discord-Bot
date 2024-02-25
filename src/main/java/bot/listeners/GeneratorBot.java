package bot.listeners;

import bot.utils.dungeon.data.Dungeon;
import com.zgamelogic.annotations.DiscordController;
import com.zgamelogic.annotations.DiscordMapping;
import data.database.guildData.GuildData;
import data.database.guildData.GuildDataRepository;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.FileUpload;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static bot.utils.dungeon.DungeonGenerator.*;

@DiscordController
public class GeneratorBot {

    private final GuildDataRepository guildData;

    @Autowired
    public GeneratorBot(GuildDataRepository guildData) {
        this.guildData = guildData;
    }

    @DiscordMapping(Id = "enable_generator")
    private void enableGenerator(ButtonInteractionEvent event){
        event.editButton(Button.success("disable_generator", "Generator bot")).queue();
        Guild guild = event.getGuild();
        long dungeonCommandId = guild.upsertCommand(
                Commands.slash("generate", "Generator commands")
                        .addSubcommands(
                                new SubcommandData("dungeon", "Dungeon generator")
                                        .addOption(OptionType.STRING, "size", "Size of the dungeon", false, true)
                        )
        ).complete().getIdLong();
        GuildData dbGuild = guildData.getReferenceById(guild.getIdLong());
        dbGuild.setGeneratorEnabled(true);
        dbGuild.setGenerateDungeonCommandId(dungeonCommandId);
        guildData.save(dbGuild);
    }

    @DiscordMapping(Id = "disable_generator")
    private void disableGenerator(ButtonInteractionEvent event){
        event.editButton(Button.danger("enable_generator", "Generator bot")).queue();
        Guild guild = event.getGuild();
        GuildData dbGuild = guildData.getReferenceById(guild.getIdLong());
        dbGuild.setGeneratorEnabled(false);
        guild.deleteCommandById(dbGuild.getGenerateDungeonCommandId()).queue();
        dbGuild.setGenerateDungeonCommandId(null);
        guildData.save(dbGuild);
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
