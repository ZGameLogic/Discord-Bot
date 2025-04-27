package com.zgamelogic.bot.utils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;

public abstract class CobbleHelper {
    private final static Color COBBLE_COLOR = new Color(149, 145, 145);

    public static MessageEmbed getHelpMessage(){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(COBBLE_COLOR);
        eb.setThumbnail("attachment://cobble-logo.png");
        eb.setTitle("Cobble Help");
        eb.setDescription("This is a test description");
        eb.setFooter("This is a footer");
        eb.setAuthor("Author");
        return eb.build();
    }
}
