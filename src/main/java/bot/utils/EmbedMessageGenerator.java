package bot.utils;

import data.database.cardData.cards.CardData;
import data.database.cardData.cards.CardDataRepository;
import data.database.cardData.player.PlayerCardData;
import data.database.planData.Plan;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.util.LinkedList;

import static bot.listeners.CardBot.PAGE_SIZE;

public abstract class EmbedMessageGenerator {

    private final static Color GENERAL_COLOR = new Color(99, 42, 129);
    private final static Color CARD_COLOR = new Color(43, 97, 158);

    public static MessageEmbed cardShopMessage(long userId, CardData card, int price){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(CARD_COLOR);
        eb.setTitle(card.getName() + " for sale!");
        eb.setDescription(card.toDiscordMessage(true));
        eb.addField("Price", price + "", true);
        eb.setFooter(userId + "");
        return eb.build();
    }

    public static MessageEmbed cardPackOpen(String username, LinkedList<Long> newCards, LinkedList<Long> dupCards, CardDataRepository cards, int moneyMade){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(CARD_COLOR);
        eb.setTitle("Card opening for " + username);
        StringBuilder desc = new StringBuilder("New cards\n==========\n");
        for(long id: newCards){
            desc.append(cards.getOne(id).toDiscordMessage(true)).append("\n");
        }
        desc.append("\nDuplicate cards\n===============\n");
        for(long id: dupCards){
            CardData card = cards.getOne(id);
            desc.append(card.toDiscordMessage(true)).append(" +").append(card.getSellback()).append(" pip\n");
        }
        eb.setDescription(desc.toString());
        eb.addField("Money made", moneyMade + "", true);
        return eb.build();
    }

    public static MessageEmbed cardPlayerStatus(String user, PlayerCardData player){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(CARD_COLOR);
        eb.setTitle("Status for " + user);
        StringBuilder desc = new StringBuilder();
        for(String collection: player.getPacks().keySet()){
            desc.append(collection).append(": ").append(player.getPacks().get(collection)).append("\n");
        }
        eb.setDescription(desc.toString());
        eb.addField("Pips", player.getCurrency() + "", true);
        String progress;
        if(player.getProgress() >= 3600){
            progress = "Ready to redeem";
        } else if (player.getProgress() >= 3300){
            progress = "Almost there";
        } else if (player.getProgress() >= 1800){
            progress = "Halfway there";
        } else {
            progress = "Not close";
        }
        eb.addField("Free pack progress", progress, true);
        return eb.build();
    }

    public static MessageEmbed overallCollectionView(String user, LinkedList<Long> deck, CardDataRepository cards){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(CARD_COLOR);
        eb.setTitle(user + "'s collection stats");
        StringBuilder desc = new StringBuilder();
        for(String collection: cards.listCardCollections()){ // go through each collection
            LinkedList<CardData> cardsInCollection = cards.findCardsByCollection(collection);
            int total = cardsInCollection.size(); // get total number of cards in the collection
            int userTotal = 0; // number of cards the user has in the collection
            for(long cardId: deck){
                if(cardsInCollection.contains(new CardData().setId(cardId))) userTotal++;
            }
            desc.append(collection).append(": ").append(userTotal).append("/").append(total).append("\n");
        }
        eb.setDescription(desc.toString());
        return eb.build();
    }

    public static MessageEmbed specificCollectionView(User user, String collectionName, LinkedList<Long> deck, LinkedList<CardData> cardsInCollection, int page){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(CARD_COLOR);
        eb.setTitle(user.getName() + "'s " + collectionName + " collection");
        StringBuilder desc = new StringBuilder();
        desc.append("<@").append(user.getId()).append(">\n");
        int total = cardsInCollection.size(); // get total number of cards in the collection
        int userTotal = 0; // number of cards the user has in the collection
        int index = 0;
        for(CardData card: cardsInCollection){
            if(index >= page * PAGE_SIZE && index < (page + 1) * PAGE_SIZE) {
                desc.append(card.toDiscordMessage(false)).append(": \t").append(deck.contains(card.getId()) ? "collected" : "not collected").append("\n");
            }
            index++;
            if(deck.contains(card.getId())) userTotal++;
        }
        desc.append("Collected ").append(userTotal).append(" out of ").append(total).append(" cards in the collection");
        eb.setDescription(desc.toString());
        if(total > PAGE_SIZE) eb.setFooter("page " + (page + 1));
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
