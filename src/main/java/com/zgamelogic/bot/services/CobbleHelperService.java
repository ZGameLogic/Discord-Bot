package com.zgamelogic.bot.services;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.stereotype.Service;

import java.awt.*;

@Service
@RequiredArgsConstructor
public class CobbleHelperService {
    private final CobbleEmojiService ces;

    public final String PAGEABLE_PERMISSION = "You do not have permissions to change the page on this message.";
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
                        ces.km("production"),
                        ces.km("rations"),
                        ces.km("wood"),
                        ces.km("stone"),
                        ces.km("metal"),
                        ces.km("magic")
                ));
                break;
            case 3:
                eb.setTitle("Cobble Help - building");
                eb.setDescription(BUILDING_DESCRIPTION);
                eb.addField("Wheat farm",  "1x" + ces.km("production") + " -> " + ces.km("rations"), true);
                break;
            case 1:
            default:
                eb.setTitle("Cobble Help");
                eb.setDescription("This is a test description");
                break;
        }
        eb.setThumbnail("attachment://cobble-logo.png");
        eb.setFooter("Page " + page);
        return eb.build();
    }
}
