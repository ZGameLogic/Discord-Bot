package com.zgamelogic.bot.utils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;

public abstract class CobbleHelper {
    public static final String PAGEABLE_PERMISSION = "You do not have permissions to change the page on this message.";

    private final static Color COBBLE_COLOR = new Color(149, 145, 145);

    public static MessageEmbed getHelpMessage(int page){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(COBBLE_COLOR);
        switch (page){
            case 2:
                eb.setTitle("Cobble Help - resources");
                eb.setDescription("This is a test description for resources");
                break;
            case 3:
                eb.setTitle("Cobble Help - building");
                eb.setDescription("This is a test description for building");
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
