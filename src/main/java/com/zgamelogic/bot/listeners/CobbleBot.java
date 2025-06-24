package com.zgamelogic.bot.listeners;

import com.zgamelogic.bot.services.CobbleBotHelperService;
import com.zgamelogic.bot.services.CobbleResourceService;
import com.zgamelogic.data.database.cobbleData.enums.CobbleBuildingType;
import com.zgamelogic.data.database.cobbleData.CobbleServiceException;
import com.zgamelogic.data.database.cobbleData.building.CobbleBuilding;
import com.zgamelogic.data.database.cobbleData.building.CobbleBuildingRepository;
import com.zgamelogic.data.database.cobbleData.npc.CobbleNpc;
import com.zgamelogic.data.database.cobbleData.player.CobblePlayer;
import com.zgamelogic.discord.annotations.DiscordController;
import com.zgamelogic.discord.annotations.DiscordMapping;
import com.zgamelogic.discord.annotations.EventProperty;
import com.zgamelogic.services.CobbleService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.FileUpload;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@DiscordController
@AllArgsConstructor
public class CobbleBot {
    private final CobbleBotHelperService helperService;
    private final CobbleResourceService resourceService;
    private final CobbleService cobbleService;
    private final CobbleBotHelperService cobbleBotHelperService;
    private final CobbleBuildingRepository cobbleBuildingRepository;

    @DiscordMapping(Id = "cobble", SubId = "help")
    private void cobbleHelp(SlashCommandInteractionEvent event) throws IOException {
        event
            .replyFiles(FileUpload.fromData(resourceService.getCobbleLogo().getInputStream(), "cobble-logo.png"))
            .addEmbeds(helperService.getHelpMessage(1))
                .addActionRow(Button.secondary("cobble-help-page-prev", "Previous page").asDisabled(), Button.secondary("cobble-help-page-next", "Next Page"))
            .queue();
    }

    @DiscordMapping(Id = "cobble", SubId = "start")
    private void cobbleStart(SlashCommandInteractionEvent event, @EventProperty(name = "town-name") String townName) {
        try {
            if(townName == null || townName.isEmpty()) townName = event.getUser().getName() + "'s town";
            CobblePlayer player = cobbleService.startCobblePlayer(event.getUser().getIdLong(), townName);
            event
                .replyFiles(FileUpload.fromData(resourceService.mapAppearanceAsStream(player.getNpcs().get(0).getAppearance()), "npc.png"))
                .addEmbeds(helperService.getStartMessage(player))
                .queue();
        } catch (CobbleServiceException | IOException e) {
            event.reply(e.getMessage()).setEphemeral(true).queue();
        }
    }

    @DiscordMapping(Id = "cobble", SubId = "citizens")
    private void cobbleCitizens(
        SlashCommandInteractionEvent event,
        @EventProperty String citizen
    ) {
        boolean citizenIncluded = citizen != null && !citizen.isEmpty();
        try {
            if (citizenIncluded) {
                cobbleBotHelperService.cobbleCitizen(event, citizen);
            } else {
                cobbleBotHelperService.cobbleCitizens(event);
            }
        } catch (CobbleServiceException | IOException e) {
            event.reply(e.getMessage()).setEphemeral(true).queue();
        }
    }

    @DiscordMapping(Id = "cobble", SubId = "citizens", FocusedOption = "citizen")
    private void cobbleCitizensAutocomplete(
        CommandAutoCompleteInteractionEvent event,
        @EventProperty String citizen
    ){
        try {
            List<CobbleNpc> npcs = cobbleService.getCobbleNpcs(event.getUser().getIdLong());
            event.replyChoices(npcs.stream()
                .filter(npc -> citizen.isEmpty() || npc.getFirstName().toLowerCase().contains(citizen.toLowerCase()) ||  npc.getLastName().toLowerCase().contains(citizen.toLowerCase()))
                .map(npc -> new Command.Choice(npc.getFirstName() + " " + npc.getLastName(), npc.getId().toString()))
                .toList()
            ).queue();
        } catch (CobbleServiceException e) {
            event.replyChoices(List.of()).queue();
        }
    }

    @DiscordMapping(Id = "cobble", GroupName = "building", SubId = "codex", FocusedOption = "building")
    private void cobbleBuildingCodexAutocomplete(
        CommandAutoCompleteInteractionEvent event,
        @EventProperty String building
    ){
        event.replyChoices(Arrays.stream(CobbleBuildingType.values())
            .filter(type -> building.isEmpty() || type.getFriendlyName().toLowerCase().replaceAll("'", "").contains(building.toLowerCase()))
            .map(type -> new Command.Choice(type.getFriendlyName(), type.getFriendlyName()))
            .toList()
        ).queue();
    }

    @DiscordMapping(Id = "cobble", GroupName = "building", SubId = "codex")
    private void cobbleBuildingCodex(
        SlashCommandInteractionEvent event,
        @EventProperty String building
    ) throws CobbleServiceException {
        int page = 1;
        int maxPage = cobbleService.getCobbleBuildingList().size();
        if(building != null && CobbleBuildingType.validName(building)) {
            page = CobbleBuildingType.fromName(building).ordinal() + 1;
        }
        event.replyEmbeds(helperService.getBuildingMessage(page))
            .addActionRow(
                Button.secondary("cobble-building-codex-page-prev", "Previous page").withDisabled(page == 1),
                Button.secondary("cobble-building-codex-page-next", "Next Page").withDisabled(page == maxPage)
            )
        .queue();
    }

    @DiscordMapping(Id = "cobble", GroupName = "rename", SubId = "building", FocusedOption = "name")
    private void cobbleRenameBuildingAutocomplete(
        CommandAutoCompleteInteractionEvent event,
        @EventProperty String building
    ){
        event.replyChoices(cobbleBuildingRepository.findAllByPlayer_PlayerId(event.getUser().getIdLong()).stream()
            .filter(cb -> building == null || building.isEmpty() || cb.getBuildingName().toLowerCase().contains(building.toLowerCase()))
            .map(cb -> new Command.Choice(cb.getBuildingName(), cb.getCobbleBuildingId().toString()))
            .toList()
        ).queue();
    }

    @DiscordMapping(Id = "cobble", GroupName = "rename", SubId = "building")
    private void cobbleRenameBuildingSlashCommand(
        SlashCommandInteractionEvent event,
        @EventProperty String name,
        @EventProperty(name = "new-name") String newName
    ){
        try {
            CobbleBuilding building = cobbleService.getCobbleBuilding(event.getUser().getIdLong(), name);
            cobbleService.renameBuilding(building, newName);
            event.reply("Building successfully renamed to " + newName).setEphemeral(true).queue();
        } catch (CobbleServiceException e) {
            event.reply(e.getMessage()).setEphemeral(true).queue();
        }
    }

    @DiscordMapping(Id = "cobble", GroupName = "rename", SubId = "town")
    private void cobbleRenameTownSlashCommand(
        SlashCommandInteractionEvent event,
        @EventProperty(name = "new-name") String name
    ){
        try {
            CobblePlayer player = cobbleService.getCobblePlayer(event.getUser().getIdLong());
            cobbleService.renameTown(player, name);
            event.reply("Town successfully renamed to " + name).setEphemeral(true).queue();
        } catch (CobbleServiceException e) {
            event.reply(e.getMessage()).setEphemeral(true).queue();
        }
    }

    @DiscordMapping(Id = "cobble-help-page-next")
    private void cobbleHelpPageNext(ButtonInteractionEvent event){ helperService.cobbleHelpPage(event); }
    @DiscordMapping(Id = "cobble-help-page-prev")
    private void cobbleHelpPagePrev(ButtonInteractionEvent event){ helperService.cobbleHelpPage(event); }

    @DiscordMapping(Id = "cobble-building-codex-page-next")
    private void cobbleBuildingCodexPageNext(ButtonInteractionEvent event){ helperService.cobbleBuildingCodexPage(event); }
    @DiscordMapping(Id = "cobble-building-codex-page-prev")
    private void cobbleBuildingCodexPagePrev(ButtonInteractionEvent event){ helperService.cobbleBuildingCodexPage(event); }

    @Bean
    public List<CommandData> cobbleCommands(){
        return List.of(
            Commands.slash("cobble", "All commands related to the cobble game.").addSubcommands(
                new SubcommandData("help", "Get some idea on how to play the game."),
                new SubcommandData("start", "Start the game of cobble!")
                    .addOption(OptionType.STRING, "town-name", "The name of your town", false),
                new SubcommandData("production", "Get an overview of production statistics for your town"),
                new SubcommandData("citizens", "Get information on citizens in your town")
                    .addOption(OptionType.STRING, "citizen", "Get a specific citizen", false, true)
            ).addSubcommandGroups(
                new SubcommandGroupData("building", "building stuff").addSubcommands(
                    new SubcommandData("codex", "Get a book of buildings and what they do")
                        .addOption(OptionType.STRING, "building", "Start the book on a specific building", false, true)
                ),
                new SubcommandGroupData("rename", "rename stuff").addSubcommands(
                    new SubcommandData("building", "Rename a building")
                        .addOption(OptionType.STRING, "name", "The current name of the building", true, true)
                        .addOption(OptionType.STRING, "new-name", "The new name of the building", true),
                    new SubcommandData("town", "Rename your town")
                        .addOption(OptionType.STRING, "new-name", "The new name of the town", true)
                ),
                new SubcommandGroupData("schedule", "Schedule things to happen").addSubcommands(
                    new SubcommandData("build", "Schedule a building to be built during the day")
                        .addOption(OptionType.STRING, "building", "The building to be scheduled to be built", true, true)
                        .addOption(OptionType.STRING, "npc", "The NPC to assign to this building when built", true, true),
                    new SubcommandData("upgrade", "Schedule a building to be upgraded during the day")
                        .addOption(OptionType.STRING, "building", "The building to be scheduled to be upgraded", true, true)
                        .addOption(OptionType.STRING, "npc", "The NPC to assign to this building after upgrade if required", false, true)
                )
            )
        );
    }
}
