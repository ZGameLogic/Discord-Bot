package bot.utils;

import data.database.planData.Plan;
import data.database.planData.User;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.util.LinkedList;

import static bot.utils.EmbedMessageGenerator.GENERAL_COLOR;

public abstract class PlanHelper {

    public static MessageEmbed getHostMessage(Plan plan, Guild guild){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(GENERAL_COLOR);
        eb.setTitle("Plan details for: " + plan.getTitle());
        eb.setDescription("The event is scheduled for " + TimeFormat.DATE_TIME_SHORT.format(plan.getDate().getTime()) + "\n" +
                plan.getNotes() + "\n" + plan.getLog() != null ? plan.getLog() : "");
        infoBody(plan, eb);
        eb.setFooter(String.valueOf(plan.getId()));
        return eb.build();
    }

    public static MessageEmbed getPlanChannelMessage(Plan plan, Guild guild){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(GENERAL_COLOR);
        eb.setTitle(plan.getTitle());
        eb.setDescription("The event is scheduled for " + TimeFormat.DATE_TIME_SHORT.format(plan.getDate().getTime()) + "\n" +
                plan.getNotes());
        eb.setFooter(String.valueOf(plan.getId()));
        eb.addField("Coordinator", guild.getJDA().getUserById(plan.getAuthorId()).getName(), true);
        eb.addField("People accepted", plan.getAccepted().size() +
                        (plan.getCount() != -1 ? "/" + plan.getCount() : "")
                , true);
        infoBody(plan, eb);
        return eb.build();
    }

    public static MessageEmbed getPlanPrivateMessage(Plan plan, Guild guild){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(GENERAL_COLOR);
        String inviter = guild.getJDA().getUserById(plan.getAuthorId()).getName();
        int count = plan.getCount();
        eb.setTitle(inviter + " has invited you to join them doing: " + plan.getTitle());
        String desc = inviter + " is looking for " +
                (plan.getCount() == -1 ? "" : count) +
                " people to join them for " + plan.getTitle() + " (" + plan.getInvitees().size() + " invited).\n" +
                "The event is scheduled for " + TimeFormat.DATE_TIME_SHORT.format(plan.getDate().getTime()) + "\n" +
                "For more details, visit the planning channel in the discord server: " + guild.getName() + "\n" + plan.getNotes();
        if(plan.isFull() && !plan.isNeedFillIn()){
            desc += "\nWe are no longer looking for more members to join this event. Check back later in case someone drops out or requests a fill-in.\nYou can also waitlist yourself so if anyone does drop out or requests a fill-in, you will join the event automatically.";
        } else if(plan.isFull() && plan.isNeedFillIn()){
            desc += "\nWe are looking for some fill-ins for the event.";
        }
        eb.setDescription(desc);
        StringBuilder attendees = new StringBuilder();
        int filledInIndex = 0;
        for(Long id: plan.getAccepted()){
            attendees.append("<@").append(id).append(">");
            if(plan.requestedFillIn(id)){
                if(plan.getFillInedList().size() > filledInIndex){
                    attendees.append(" -> (<@").append(plan.getFillInedList().get(filledInIndex++)).append(">)");
                } else {
                    attendees.append(" (Requested fill-in)");
                }
            }
            attendees.append("\n");
        }
        if(attendees.isEmpty()) attendees = new StringBuilder("People who accept will show up here");
        eb.addField("People accepted " + plan.getAccepted().size() + (plan.getCount() != -1 ? "/" + plan.getCount() : ""), attendees.toString(), true);
        if(!plan.getWaitlist().isEmpty()){
            StringBuilder waitlistees = new StringBuilder();
            for(Long id: plan.getWaitlist()){
                waitlistees.append("<@").append(id).append(">").append("\n");
            }
            eb.addField("Wait list", waitlistees.toString(), true);
        }
        if(!plan.getMaybes().isEmpty()){
            StringBuilder maybes = new StringBuilder();
            for(Long id: plan.getMaybes()){
                maybes.append("<@").append(id).append(">").append("\n");
            }
            eb.addField("Maybes", maybes.toString(), true);
        }
        eb.setFooter(String.valueOf(plan.getId()));
        return eb.build();
    }

    private static void infoBody(Plan plan, EmbedBuilder eb) {
        StringBuilder status = new StringBuilder();
        int accepted = plan.getAccepted().size();
        if(plan.getCount() != -1) {
            status.append("filled:|`");
            for (int i = 0; i < 20; i++) {
                if (i < 20.0 * ((double) accepted / plan.getCount()) || plan.isFull()) {
                    status.append("█");
                } else {
                    status.append(" ");
                }
            }
            status.append("`|\n");
        }
        int filledInIndex = 0;
        for(long id: plan.getAccepted()){
            status.append("<@").append(id).append(">").append(": accepted");
            if(plan.requestedFillIn(id)){
                if(plan.getFillInedList().size() > filledInIndex){
                    status.append(" -> (<@").append(plan.getFillInedList().get(filledInIndex++)).append(">)");
                } else {
                    status.append(" (Requested fill-in)");
                }
            }
            status.append("\n");
        }
        for(long id: plan.getWaitlist()){
            status.append("<@").append(id).append(">").append(": wait listed\n");
        }
        for(long id: plan.getMaybes()){
            status.append("<@").append(id).append(">").append(": maybe\n");
        }
        for(long id: plan.getPending()){
            status.append("<@").append(id).append(">").append(": pending invite\n");
        }
        for(long id: plan.getDeclined()){
            status.append("<@").append(id).append(">").append(": declined\n");
        }
        eb.addField("Invite status", status.toString(), false);
    }

    public static LinkedList<Button> getButtons(boolean full, boolean needsFillIn, User.Status status, boolean requestedFillIn){
        LinkedList<Button> neededButtons = new LinkedList<>();

        Button accept = Button.success("accept_event", "Accept");
        Button deny = Button.danger("deny_event", "Deny");
        Button maybe = Button.primary("maybe_event", "Maybe");
        Button waitlist = Button.secondary("waitlist_event", "Waitlist");
        Button dropout = Button.danger("drop_out_event", "Drop out");
        Button requestFillIn = Button.primary("request_fill_in", "Request fill in");
        Button fillIn = Button.secondary("fill_in", "Fill in");

        switch(status){
            case DECIDING:
                if(full) {
                    if(needsFillIn){
                        neededButtons.add(fillIn);
                        neededButtons.add(maybe);
                        neededButtons.add(deny);
                    } else {
                        neededButtons.add(waitlist);
                        neededButtons.add(maybe);
                        neededButtons.add(deny);
                    }
                } else {
                    neededButtons.add(accept);
                    neededButtons.add(maybe);
                    neededButtons.add(deny);
                }
                break;
            case ACCEPTED:
                if(!requestedFillIn) neededButtons.add(requestFillIn);
                neededButtons.add(dropout);
                break;
            case MAYBED:
                if(full) {
                    if(needsFillIn){
                        neededButtons.add(fillIn);
                        neededButtons.add(deny);
                    } else {
                        neededButtons.add(waitlist);
                        neededButtons.add(deny);
                    }
                } else {
                    neededButtons.add(accept);
                    neededButtons.add(deny);
                }
                break;
            case WAITLISTED:
            case FILLINED:
                neededButtons.add(dropout);
                break;
            case DECLINED:
                break;
        }

        return neededButtons;
    }
}
