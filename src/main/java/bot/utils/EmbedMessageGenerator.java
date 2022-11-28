package bot.utils;

import data.database.cardData.cards.CardData;
import data.database.cardData.cards.CardDataRepository;
import data.database.cardData.player.PlayerCardData;
import data.database.planData.Plan;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.LinkedList;

public abstract class EmbedMessageGenerator {

    private final static Color GENERAL_COLOR = new Color(99, 42, 129);
    private final static Color CARD_COLOR = new Color(43, 97, 158);

    public static MessageEmbed cardPlayerStatus(String user, PlayerCardData player){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(CARD_COLOR);
        eb.setTitle("Status for " + user);
        String desc = "";
        for(String collection: player.getPacks().keySet()){
            desc += collection + ": " + player.getPacks().get(collection) + "\n";
        }
        eb.setDescription(desc);
        eb.addField("Pips", player.getCurrency() + "", true);
        return eb.build();
    }

    public static MessageEmbed overallCollectionView(String user, LinkedList<Long> deck, CardDataRepository cards){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(CARD_COLOR);
        eb.setTitle(user + "'s collection stats");
        String desc = "";
        for(String collection: cards.listCardCollections()){ // go through each collection
            LinkedList<CardData> cardsInCollection = cards.findCardsByCollection(collection);
            int total = cardsInCollection.size(); // get total number of cards in the collection
            int userTotal = 0; // number of cards the user has in the collection
            for(long cardId: deck){
                if(cardsInCollection.contains(new CardData().setId(cardId))) userTotal++;
            }
            desc += collection + ": " + userTotal + "/" + total + "\n";
        }
        eb.setDescription(desc);
        return eb.build();
    }

    public static MessageEmbed specificCollectionView(String user, String collectionName, LinkedList<Long> deck, CardDataRepository cards){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(CARD_COLOR);
        eb.setTitle(user + "'s " + collectionName + " collection");
        String desc = "";
        LinkedList<CardData> cardsInCollection = cards.findCardsByCollection(collectionName);
        int total = cardsInCollection.size(); // get total number of cards in the collection
        int userTotal = 0; // number of cards the user has in the collection
        for(CardData card: cardsInCollection){
            desc += String.format("__%04d__ **", card.getId()) + card.getName() + "**: \t" + (deck.contains(card.getId()) ? "collected" : "not collected") + "\n";
            if(deck.contains(card.getId())) userTotal++;
        }
        desc += "Collected " + userTotal + " out of " + total + " cards in the collection";
        eb.setDescription(desc);
        return eb.build();
    }

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
        String inviter = guild.getJDA().getUserById(plan.getAuthorId()).getName();
        int count = plan.getCount();
        eb.setTitle(inviter + " has invited you to join them doing: " + plan.getTitle());
        String desc = inviter + " is looking for " + count + " people to join them for " + plan.getTitle() + " (" + plan.getInvitees().size() + " invited).\n" +
                "For more details, visit the planning channel in the discord server: " + guild.getName() + "\n" + plan.getNotes();
        if(plan.getAccepted().size() >= count){
            desc += "\nWe are no longer looking for more members to join this event. Check back later in case someone drops out.\nYou can also waitlist yourself so if anyone does drop out, you will join the event.";
        }
        eb.setDescription(desc);
        StringBuilder attendees = new StringBuilder();
        for(Long id: plan.getAccepted()){
            attendees.append("<@").append(id).append(">").append("\n");
        }
        if(attendees.length() == 0) attendees = new StringBuilder("People who accept will show up here");
        eb.addField("People accepted " + plan.getAccepted().size() + "/" + count, attendees.toString(), true);
        if(plan.getWaitlist().size() > 0){
            StringBuilder waitlistees = new StringBuilder();
            for(Long id: plan.getWaitlist()){
                waitlistees.append("<@").append(id).append(">").append("\n");
            }
            eb.addField("Wait list", waitlistees.toString(), true);
        }
        if(plan.getMaybes().size() > 0){
            StringBuilder maybes = new StringBuilder();
            for(Long id: plan.getMaybes()){
                maybes.append("<@").append(id).append(">").append("\n");
            }
            eb.addField("Maybes", maybes.toString(), true);
        }
        eb.setFooter(plan.getId() + "");
        return eb.build();
    }

    public static MessageEmbed creatorMessage(Plan plan, Guild guild){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Plan details for: " + plan.getTitle());
        eb.setDescription(plan.getNotes() + "\n" + plan.getLog());
        infoBody(plan, guild, eb);
        eb.setFooter(plan.getId() + "");
        return eb.build();
    }

    private static void infoBody(Plan plan, Guild guild, EmbedBuilder eb) {
        StringBuilder status = new StringBuilder();
//        int accepted = plan.getAccepted().size();
//        status.append("filled:`");
//        for(int i = 1; i <= 20; i++){
//            if(i < Math.round(20.0 / plan.getCount() * accepted) || plan.isFull()){
//                status.append("█");
//            } else {
//                status.append(" ");
//            }
//        }
//        status.append("`\n");
        for(long id: plan.getAccepted()){
            status.append("<@").append(id).append(">").append(": accepted\n");
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

    /**
     * Use this for creating a message for the plan message for the guild
     * @return An embed message
     */
    public static MessageEmbed guildPublicMessage(Plan plan, Guild guild){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(GENERAL_COLOR);
        eb.setTitle(plan.getTitle());
        eb.setDescription(plan.getNotes());
        eb.setFooter(plan.getId() + "");
        eb.addField("Coordinator", guild.getJDA().getUserById(plan.getAuthorId()).getName(), true);
        eb.addField("People accepted", plan.getAccepted().size() + "/" + plan.getCount(), true);
        infoBody(plan, guild, eb);
        return eb.build();
    }
}
