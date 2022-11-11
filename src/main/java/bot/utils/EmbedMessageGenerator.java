package bot.utils;

import data.database.planData.Plan;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
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
     * @return An embed message
     */
    public static MessageEmbed singleInvite(Plan plan, Guild guild){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(GENERAL_COLOR);
        String inviter = guild.getMemberById(plan.getAuthorId()).getEffectiveName();
        int count = plan.getCount();
        eb.setTitle(inviter + " has invited you to join them doing: " + plan.getTitle());
        eb.setDescription(inviter + " is looking for " + count + " people to join them for " + plan.getTitle() + " (" + plan.getInvitees().size() + " invited).\n" + plan.getNotes());
        eb.addField("People accepted", plan.getAccepted().size() + "/" + count, true);
        String attendees = "";
        for(Long id: plan.getAccepted()){
            String name = guild.getMemberById(id).getEffectiveName();
            attendees += name + "\n";
        }
        if(attendees.isEmpty()) attendees = "People who accept will show up here";
        eb.addField("People attending", attendees, false);
        eb.setFooter(plan.getId() + "");
        return eb.build();
    }

    /**
     * Use this for creating a message for the plan message
     * @return An embed message
     */
    public static MessageEmbed plan(Plan plan, Guild guild){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(GENERAL_COLOR);
        eb.setTitle(plan.getTitle());
        eb.setDescription(plan.getNotes());
        eb.addField("Coordinator", guild.getMemberById(plan.getAuthorId()).getEffectiveName(), true);
        String status = "";
        for(long id: plan.getAccepted()){
            String name = guild.getMemberById(id).getEffectiveName();
            status += name + ": accepted\n";
        }
        for(long id: plan.getPending()){
            String name = guild.getMemberById(id).getEffectiveName();
            status += name + ": pending invite\n";
        }
        for(long id: plan.getDeclined()){
            String name = guild.getMemberById(id).getEffectiveName();
            status += name + ": declined\n";
        }
        eb.addField("Invite status", status, false);
        return eb.build();
    }
}
