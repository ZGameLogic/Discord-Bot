package bot.listeners;

import bot.utils.dungeon.data.Dungeon;
import com.zgamelogic.AdvancedListenerAdapter;
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

import java.io.File;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static bot.utils.dungeon.DungeonGenerator.*;

public class GeneratorBot extends AdvancedListenerAdapter {

    private final GuildDataRepository guildData;

    public GeneratorBot(GuildDataRepository guildData) {
        this.guildData = guildData;
    }

    @ButtonResponse("enable_generator")
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
        GuildData dbGuild = guildData.getOne(guild.getIdLong());
        dbGuild.setGeneratorEnabled(true);
        dbGuild.setGenerateDungeonCommandId(dungeonCommandId);
        guildData.save(dbGuild);
    }

    @ButtonResponse("disable_generator")
    private void disableGenerator(ButtonInteractionEvent event){
        event.editButton(Button.danger("enable_generator", "Generator bot")).queue();
        Guild guild = event.getGuild();
        GuildData dbGuild = guildData.getOne(guild.getIdLong());
        dbGuild.setGeneratorEnabled(false);
        guild.deleteCommandById(dbGuild.getGenerateDungeonCommandId()).queue();
        dbGuild.setGenerateDungeonCommandId(null);
        guildData.save(dbGuild);
    }

    @AutoCompleteResponse(slashCommandId = "generate", slashSubCommandId = "dungeon", focusedOption = "size")
    private void dungeonSizeAutoCompleteResponse(CommandAutoCompleteInteractionEvent event){
        event.replyChoices(
                Stream.of(new String[]{"small", "medium", "large"})
                .map(word -> new Command.Choice(word, word))
                .collect(Collectors.toList())
        ).queue();
    }

    @SlashResponse(value = "generate", subCommandName = "dungeon")
    private void generateDungeonSlashCommand(SlashCommandInteractionEvent event){
        event.deferReply().queue();
        Dungeon dungeon;
        if(event.getOption("size") == null) {
            dungeon = GenerateRandomDungeon();
        } else {
            switch(event.getOption("size").getAsString()){
                case "small":
                    dungeon = GenerateDungeon(Size.SMALL);
                    break;
                case "medium":
                    dungeon = GenerateDungeon(Size.MEDIUM);
                    break;
                case "large":
                    dungeon = GenerateDungeon(Size.LARGE);
                    break;
                default:
                    dungeon = GenerateRandomDungeon();
            }
        }
        File file = saveDungeon(dungeon);
        event.getHook().sendFiles(FileUpload.fromData(file)).complete();
        file.delete();
    }
}
