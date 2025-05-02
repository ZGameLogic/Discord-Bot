package com.zgamelogic.bot.services;

import com.zgamelogic.data.database.cobbleData.player.CobblePlayer;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.stereotype.Service;

import java.awt.*;

@Service
@RequiredArgsConstructor
public class CobbleHelperService {
    private final CobbleResourceService ces;

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
                eb.setDescription(BUILDING_DESCRIPTION);
                eb.addField("Wheat farm",  ces.em("production") + " -> " + ces.em("rations"), true);
                eb.addField("Fishery",  ces.em("production") + " -> " + ces.em("rations"), true);
                eb.addField("Builder",  ces.em("production") + " -> work", true);
                eb.addField("Mine",  ces.em("production") + ces.em("production") + ces.em("wood") +  " -> " + ces.em("stone") + ces.em("stone") + ces.em("metal"), true);
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

    public MessageEmbed getStartMessage(CobblePlayer player) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(COBBLE_COLOR);
        eb.setTitle("Cobble Start");
        return eb.build();
    }
}
