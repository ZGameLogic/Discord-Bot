package com.zgamelogic.bot.services;

import com.zgamelogic.data.database.cobbleData.CobbleBuildingType;
import com.zgamelogic.data.database.cobbleData.CobbleServiceException;
import com.zgamelogic.data.database.cobbleData.player.CobblePlayer;
import com.zgamelogic.data.database.cobbleData.production.CobbleProduction;
import com.zgamelogic.services.CobbleService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class CobbleBotHelperService {
    private final CobbleResourceService ces;
    private final CobbleService cobbleService;

    public final String PAGEABLE_PERMISSION = "You do not have permissions to change the page on this message.";
    public final String COBBLE_DESCRIPTION = """
        Cobble is a colony simulator where you produce to consume. Every day (12 hours real world time) your colony will produce and consume an amount of resources.
        This is determined by the buildings you build and the number of citizens you have. Progression comes in the form of upgrading your buildings to produce more.
        Your town hall building level determines what buildings you can build and what level you can upgrade them to.
        (Coming soon) A lightweight desktop client to just throw up and watch your colony produce.
        """;
    public final String RESOURCE_DESCRIPTION = """
        %s Production is a way to track how much work citizens can do each day.
        %s Rations are used to feed citizens each day.
        %s Wood is useful for building and you will need a lot of it.
        %s Stone is useful for building and upgrading.
        %s Metal is useful for defending your town and late game upgrades.
        %s Magic is useful for defending your town.
        """;
    public final String BUILDING_DESCRIPTION = """
        Buildings take resources and produce resources during the day. Some buildings take only time while others require a resource to produce.
        To get a list of buildings and what they produce, use the %s slash command.
        """;

    private final Color COBBLE_COLOR = new Color(149, 145, 145);

    public MessageEmbed getHelpMessage(int page){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(COBBLE_COLOR);
        switch (page){
            case 2:
                eb.setTitle("Cobble Help - resources");
                eb.setDescription(String.format(RESOURCE_DESCRIPTION,
                    ces.em("production"),
                    ces.em("rations"),
                    ces.em("wood"),
                    ces.em("stone"),
                    ces.em("metal"),
                    ces.em("magic")
                ));
                break;
            case 3:
                eb.setTitle("Cobble Help - building");
                eb.setDescription(String.format(BUILDING_DESCRIPTION, ces.cm("cobble building-codex")));
                break;
            case 1:
            default:
                eb.setTitle("Cobble Help");
                eb.setDescription(COBBLE_DESCRIPTION);
                break;
        }
        eb.setThumbnail("attachment://cobble-logo.png");
        eb.setFooter("Page " + page);
        return eb.build();
    }

    public MessageEmbed getBuildingMessage(int page){
        CobbleBuildingType type = CobbleBuildingType.values()[page - 1];
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(type.getFriendlyName());
        cobbleService.getCobbleProductions(type).stream()
            .sorted(Comparator.comparingInt(lhs -> lhs.getId().getLevel()))
            .forEach(production-> eb.addField("Level " + production.getId().getLevel(), "Cost:\n" + mentionableCost(production) + "\nConsumption/Production:\n" + mentionableProduction(production), true));
        eb.setFooter("Page " + page);
        return eb.build();
    }

    public MessageEmbed getStartMessage(CobblePlayer player) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(COBBLE_COLOR);
        eb.setTitle("Cobble Start");
        // TODO give them information about their 2 new NPCs
        return eb.build();
    }

    public void cobbleHelpPage(ButtonInteractionEvent event) {
        long slashUserId = event.getMessage().getInteractionMetadata().getUser().getIdLong();
        if(event.getUser().getIdLong() != slashUserId){
            event.reply(PAGEABLE_PERMISSION).setEphemeral(true).queue();
            return;
        }
        int it = event.getButton().getId().equals("cobble-help-page-next") ? 1 : -1;
        int newPage = Integer.parseInt(event.getMessage().getEmbeds().get(0).getFooter().getText().replace("Page ", "")) + it;
        event.editMessageEmbeds(getHelpMessage(newPage))
            .setActionRow(
                net.dv8tion.jda.api.interactions.components.buttons.Button.secondary("cobble-help-page-prev", "Previous page").withDisabled(newPage == 1),
                Button.secondary("cobble-help-page-next", "Next Page").withDisabled(newPage == 3)
            ).queue();
    }

    public void cobbleBuildingCodexPage(ButtonInteractionEvent event) {
        long slashUserId = event.getMessage().getInteractionMetadata().getUser().getIdLong();
        if(event.getUser().getIdLong() != slashUserId){
            event.reply(PAGEABLE_PERMISSION).setEphemeral(true).queue();
            return;
        }
        int maxPage = cobbleService.getCobbleBuildingList().size();
        int it = event.getButton().getId().equals("cobble-building-codex-page-next") ? 1 : -1;
        int newPage = Integer.parseInt(event.getMessage().getEmbeds().get(0).getFooter().getText().replace("Page ", "")) + it;
        event.editMessageEmbeds(getBuildingMessage(newPage))
            .setActionRow(
                Button.secondary("cobble-building-codex-page-prev", "Previous page").withDisabled(newPage == 1),
                Button.secondary("cobble-building-codex-page-next", "Next Page").withDisabled(newPage == maxPage)
            ).queue();
    }

    public String mentionableProduction(CobbleProduction production) {
        // TODO map production and consumption strings to correct resources
        return "";
    }

    public String mentionableCost(CobbleProduction production) {
        // TODO map cost string to correct resources
        return "";
    }

    public void cobbleCitizen(SlashCommandInteractionEvent event, String citizen) {
        // TODO complete
    }
    public void cobbleCitizens(SlashCommandInteractionEvent event){
        // TODO complete
        try {
            cobbleService.getCobbleNpcs(event.getUser().getIdLong());
        } catch (CobbleServiceException e) {
            event.reply(e.getMessage()).setEphemeral(true).queue();
        }
    }
}
