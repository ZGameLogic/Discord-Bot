package bot.utils;

import data.database.cardData.cards.CardData;
import data.database.cardData.cards.CardDataRepository;
import data.database.cardData.player.PlayerCardData;
import data.database.planData.Plan;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.awt.*;
import java.util.LinkedList;

import static bot.listeners.CardBot.PAGE_SIZE;

public abstract class EmbedMessageGenerator {

    public final static Color GENERAL_COLOR = new Color(99, 42, 129);
    private final static Color CARD_COLOR = new Color(43, 97, 158);

    public static MessageCreateData message(data.intermediates.messaging.Message message) {
        MessageCreateBuilder mcb = new MessageCreateBuilder();
        StringBuilder content = new StringBuilder();
        message.getMentionIds().forEach(id -> {
            content.append("<@&").append(id).append(">\n");
        });
        content.append(message.getMessage());
        return mcb.mentionRoles(message.getMentionIds())
                .addContent(content.toString())
                .build();
    }

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
            desc.append(cards.findById(id).get().toDiscordMessage(true)).append("\n");
        }
        desc.append("\nDuplicate cards\n===============\n");
        for(long id: dupCards){
            CardData card = cards.findById(id).get();
            desc.append(card.toDiscordMessage(true)).append(" +").append(card.getSellback()).append(" pip\n");
        }
        if(desc.length() >= 4096){
            eb = new EmbedBuilder();
            eb.setColor(CARD_COLOR);
            eb.setTitle("You did good kid");
            eb.setDescription("You have so many cards in this opening that I cannot send the whole message. Could I send multiple? Yes. But I think we both know you don't care that much.");
            return eb.build();
        }
        eb.setDescription(desc.toString());
        eb.addField("Money made", moneyMade + "", true);
        if(!eb.isValidLength()){
            eb = new EmbedBuilder();
            eb.setColor(CARD_COLOR);
            eb.setTitle("You did good kid");
            eb.setDescription("You have so many cards in this opening that I cannot send the whole message. Could I send multiple? Yes. But I think we both know you don't care that much.");
        }
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
        StringBuilder progress = new StringBuilder("|`");
        for(int i = 0; i < 20; i++){
            if(i < (20 * (player.getProgress() / 3600.0))){
                progress.append("█");
            } else {
                progress.append(" ");
            }
        }
        progress.append("`|");
        eb.addField("Free pack progress", progress.toString(), true);
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

    private static void infoBody(Plan plan, Guild guild, EmbedBuilder eb) {
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
}
