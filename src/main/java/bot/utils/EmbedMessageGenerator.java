package bot.utils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.LinkedList;

public abstract class EmbedMessageGenerator {

    private final static Color GENERAL_COLOR = new Color(99, 42, 129);

    public static MessageEmbed welcomeMessage(String ownerName, String guildName){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(GENERAL_COLOR);
        eb.setTitle("Thank you for welcoming me into " + guildName);
        eb.setDescription("Dear " + ownerName + ",\n" +
                "This channel is private and only you should be able to see it. " +
                "This bot contains multiple functions, and here is where you can enable/disable them. " +
                "Simply press the button at the bottom of this message to enable/disable a feature. " +
                "Green means its enabled, and red means its disabled. " +
                "Everything is disabled by default. As features for the bot are released, expect them " +
                "to appear under this message as a button.");
        return eb.build();
    }

    /**
     * Use this for creating a message for inviting a user for a plan
     * @param inviter Username of the person inviting
     * @param title Title of the event
     * @param notes Notes of the event
     * @param count The count of people they want in total
     * @param accepted A list of people who accepted
     * @param planId Plan Id in database
     * @param invited Count of invited users
     * @return An embed message
     */
    public static MessageEmbed singleInvite(String inviter, String title, String notes, int count, LinkedList<String> accepted, long planId, int invited){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(GENERAL_COLOR);
        eb.setTitle(inviter + " has invited you to join them doing: " + title);
        eb.setDescription(inviter + " is looking for " + count + " people to join them for " + title + " (" + invited + " invited).\n" + notes);
        eb.addField("People accepted", accepted.size() + "/" + count, true);
        String attendees = "";
        for(String name: accepted){
            attendees += name + "\n";
        }
        if(attendees.isEmpty()) attendees = "People who accept will show up here";
        eb.addField("People attending", attendees, false);
        eb.setFooter(planId + "");
        return eb.build();
    }

    /**
     * Use this for creating a message for the plan message
     * @param inviter Username of the person inviting
     * @param title Title of the event
     * @param notes Notes of the event
     * @param invited List of invited usernames
     * @param accepted List of accepted usernames
     * @param declined List of declined usernames
     * @return An embed message
     */
    public static MessageEmbed plan(String inviter, String title, String notes, LinkedList<String> invited, LinkedList<String> accepted, LinkedList<String> declined){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(GENERAL_COLOR);
        eb.setTitle(title);
        eb.setDescription(notes);
        eb.addField("Coordinator", inviter, true);
        String status = "";
        invited.removeAll(accepted);
        invited.removeAll(declined);
        for(String name: accepted){status += name + ": accepted\n";}
        for(String name: invited){ status += name + ": pending invite\n";}
        for(String name: declined){ status += name + ": declined\n";}
        eb.addField("Invite status", status, false);
        return eb.build();
    }
}
