package com.zgamelogic.bot.listeners;

import com.zgamelogic.bot.utils.EmbedMessageGenerator;
import com.zgamelogic.annotations.Bot;
import com.zgamelogic.annotations.DiscordController;
import com.zgamelogic.annotations.DiscordMapping;
import com.zgamelogic.data.database.cardData.cards.CardData;
import com.zgamelogic.data.database.cardData.cards.CardDataRepository;
import com.zgamelogic.data.database.cardData.guild.GuildCardDataRepository;
import com.zgamelogic.data.database.cardData.player.PlayerCardData;
import com.zgamelogic.data.database.cardData.player.PlayerCardDataRepository;
import com.zgamelogic.data.database.guildData.GuildDataRepository;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@DiscordController
@RestController
public class CardBot {

    public static final int PAGE_SIZE = 20;

    private final CardDataRepository cardDataRepository;
    private final GuildCardDataRepository guildCardDataRepository;
    private final PlayerCardDataRepository playerCardDataRepository;
    private final GuildDataRepository guildDataRepository;

    @Bot
    private JDA bot;

    @Value("${api.token}")
    private String apiToken;

    @Autowired
    public CardBot(GuildDataRepository guildDataRepository, CardDataRepository cardDataRepository, GuildCardDataRepository guildCardDataRepository, PlayerCardDataRepository playerCardDataRepository) {
        this.cardDataRepository = cardDataRepository;
        this.guildCardDataRepository = guildCardDataRepository;
        this.playerCardDataRepository = playerCardDataRepository;
        this.guildDataRepository = guildDataRepository;
    }

    @DiscordMapping
    public void ready(ReadyEvent event) {
        new Thread(() -> {
            List<PlayerCardData> newPlayers = new LinkedList<>();
            for(Guild guild: event.getJDA().getGuilds()){
                for(Member member: guild.getMembers()){
                    if(!playerCardDataRepository.existsById(member.getIdLong())){
                        PlayerCardData playerData = new PlayerCardData(member.getIdLong());
                        newPlayers.add(playerData);
                    }
                }
            }
            playerCardDataRepository.saveAll(newPlayers);
        }, "Card bot ready thread").start();
    }

    @DiscordMapping
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        if(guildDataRepository.findById(event.getGuild().getIdLong()).get().getCardsEnabled()) {
            if(!playerCardDataRepository.existsById(event.getUser().getIdLong()))
                playerCardDataRepository.save(new PlayerCardData(event.getUser().getIdLong()));
        }
    }

    @Bean
    private CommandData cardCommands(){
        return Commands.slash("cards", "Slash command for card bot")
                .addSubcommands(
                        new SubcommandData("collection", "View your entire collection completion")
                                .addOption(OptionType.STRING, "collection", "Specific collection data", false, true)
                                .addOption(OptionType.USER, "user", "Specific user data", false),
                        new SubcommandData("sell", "Sell a specific card you own")
                                .addOption(OptionType.STRING,"collection", "Specific collection", true, true)
                                .addOption(OptionType.STRING, "name", "Specific card name", true, true)
                                .addOption(OptionType.INTEGER, "price", "Price for card", true),
                        new SubcommandData("buy_pack", "Buy a number of packs. 100 pips each.")
                                .addOption(OptionType.INTEGER, "count", "Number of packs you want to buy", true)
                                .addOption(OptionType.STRING, "collection", "Specific collection of cards. 250 pips each.", false, true),
                        new SubcommandData("status", "View how many unopened packs you have and your pip count"),
                        new SubcommandData("open_packs", "Opens all the packs that you have")
                );
    }

    @DiscordMapping(Id = "cards", SubId = "collection", FocusedOption = "collection")
    private void collectionAutocomplete(CommandAutoCompleteInteractionEvent event){
        String[] words = cardDataRepository.listCardCollections().toArray(new String[0]);
        List<Command.Choice> options = Stream.of(words)
                .filter(word -> word.startsWith(event.getFocusedOption().getValue()))
                .map(word -> new Command.Choice(word, word))
                .collect(Collectors.toList());
        event.replyChoices(options).queue();
    }

    @DiscordMapping(Id = "cards", SubId = "buy_pack", FocusedOption = "collection")
    private void collectionBuyAutocomplete(CommandAutoCompleteInteractionEvent event){
        String[] words = cardDataRepository.listCardCollections().toArray(new String[0]);
        List<Command.Choice> options = Stream.of(words)
                .filter(word -> word.startsWith(event.getFocusedOption().getValue()))
                .map(word -> new Command.Choice(word, word))
                .collect(Collectors.toList());
        event.replyChoices(options).queue();
    }

    @DiscordMapping(Id = "cards", SubId = "sell", FocusedOption = "collection")
    private void sellCollectionAutocomplete(CommandAutoCompleteInteractionEvent event){
        String[] words = cardDataRepository.listCardCollectionsById(
                playerCardDataRepository.findById(event.getUser().getIdLong()).get().getDeck()
        ).toArray(new String[0]);
        List<Command.Choice> options = Stream.of(words)
                .filter(word -> word.startsWith(event.getFocusedOption().getValue()))
                .map(word -> new Command.Choice(word, word))
                .collect(Collectors.toList());
        event.replyChoices(options).queue();
    }

    @DiscordMapping(Id = "cards", SubId = "sell", FocusedOption = "name")
    private void sellNameAutocomplete(CommandAutoCompleteInteractionEvent event){
        String collection = event.getOption("collection") == null ? "" : event.getOption("collection").getAsString();
        Set<String> names = new HashSet<>(cardDataRepository.findByCollectionAndIds(
                playerCardDataRepository.findById(event.getUser().getIdLong()).get().getDeck(),
                collection
        ));
        String[] words = names.toArray(new String[0]);
        List<Command.Choice> options = Stream.of(words)
                .filter(word -> word.startsWith(event.getFocusedOption().getValue()))
                .map(word -> new Command.Choice(word, word))
                .collect(Collectors.toList());
        event.replyChoices(options).queue();
    }

    @DiscordMapping(Id = "cards", SubId = "sell")
    private void sellSlashCommand(SlashCommandInteractionEvent event){
        event.deferReply().queue();
        Guild guild = event.getGuild();
        TextChannel shopChannel = guild.getTextChannelById(guildCardDataRepository.findById(guild.getIdLong()).get().getShopTextChannelId());
        long user = event.getUser().getIdLong();
        LinkedList<CardData> results = cardDataRepository.findCardsByCollectionAndName(
                event.getOption("collection").getAsString(),
                event.getOption("name").getAsString()
        );
        if(results.isEmpty()){
            event.getHook().setEphemeral(true).sendMessage("That card does not exist").queue();
            return;
        }
        CardData card = results.get(0);
        if(event.getOption("price").getAsInt() <= 0){
            event.getHook().setEphemeral(true).sendMessage("Price must be positive").queue();
            return;
        }
        PlayerCardData player = playerCardDataRepository.findById(event.getUser().getIdLong()).get();
        if(!player.hasCard(card.getId())){
            event.getHook().setEphemeral(true).sendMessage("You do not own this card").queue();
            return;
        }
        player.removeCard(card.getId());
        shopChannel.sendMessageEmbeds(EmbedMessageGenerator.cardShopMessage(user, card, event.getOption("price").getAsInt()))
                .addActionRow(Button.primary("purchase_card", "Purchase")).queue();
        event.getHook().setEphemeral(true).sendMessage("Added to the <#" + shopChannel.getId() + ">").queue();
        playerCardDataRepository.save(player);
    }

    @DiscordMapping(Id = "purchase_card")
    private void purchaseCard(ButtonInteractionEvent event){
        long cardId = Long.parseLong(event.getMessage().getEmbeds().get(0).getDescription().split("__")[1]);
        long userId = Long.parseLong(event.getMessage().getEmbeds().get(0).getFooter().getText());
        int price = Integer.parseInt(event.getMessage().getEmbeds().get(0).getFields().get(0).getValue());
        PlayerCardData player = playerCardDataRepository.findById(event.getUser().getIdLong()).get();
        if(player.getDeck().contains(cardId)){
            event.reply("You already own this card").setEphemeral(true).queue();
            return;
        }
        if(!player.hasCurrency(price)){
            event.reply("You don't have enough pips to buy this card").setEphemeral(true).queue();
            return;
        }
        player.removeCurrency(price);
        player.addCard(cardId);
        event.reply("You have purchased this card").setEphemeral(true).queue();
        event.getMessage().delete().queue();
        playerCardDataRepository.save(player);
        PlayerCardData seller = playerCardDataRepository.findById(userId).get();
        seller.addCurrency(price);
        playerCardDataRepository.save(seller);
    }

    @DiscordMapping(Id = "cards", SubId = "open_packs")
    private void openPacks(SlashCommandInteractionEvent event){
        event.deferReply().queue();
        PlayerCardData player = playerCardDataRepository.findById(event.getUser().getIdLong()).get();
        LinkedList<Long> cardsInPack = new LinkedList<>();
        for(String collection: player.getPacks().keySet()){
            LinkedList<CardData> cardPool = new LinkedList<>();
            if(collection.equals("generic")){
                cardPool.addAll(cardDataRepository.findAll());
            } else {
                cardPool.addAll(cardDataRepository.findCardsByCollection(collection));
            }
            LinkedList<Long> idPool = new LinkedList<>();
            for(CardData card: cardPool){
                for(int i = 0; i < card.getRarity(); i++) idPool.add(card.getId());
            }
            for(int i = 0; i < player.getPacks().get(collection); i++){
                for(int j = 0; j < 5; j++){ // draw a card
                    cardsInPack.add(idPool.get(new Random().nextInt(idPool.size())));
                }
            }
        }
        player.removeAllPacks();
        LinkedList<Long> newCards = new LinkedList<>();
        LinkedList<Long> dupCards = new LinkedList<>();
        for(long id: cardsInPack){
            if(player.hasCard(id)){
                dupCards.add(id);
            } else if(newCards.contains(id)){
                dupCards.add(id);
            } else {
                newCards.add(id);
            }
        }
        newCards.forEach(player::addCard);
        int moneyMade = 0;
        for(long id: dupCards){
            moneyMade += cardDataRepository.findById(id).get().getSellback();
        }
        player.addCurrency(moneyMade);
        playerCardDataRepository.save(player);
        event.getHook().sendMessageEmbeds(EmbedMessageGenerator.cardPackOpen(
                event.getUser().getName(),
                newCards,
                dupCards,
                cardDataRepository,
                moneyMade
        )).queue();
    }

    @DiscordMapping(Id = "cards", SubId = "status")
    private void statusSlashCommand(SlashCommandInteractionEvent event){
        event.replyEmbeds(
                EmbedMessageGenerator.cardPlayerStatus(event.getUser().getName(), playerCardDataRepository.findById(event.getUser().getIdLong()).get())
        ).queue();
    }

    @DiscordMapping(Id = "cards", SubId = "buy_pack")
    private void buyPackSlashCommand(SlashCommandInteractionEvent event){
        if(event.getOption("count").getAsInt() <= 0) { // need a positive number of packs
            event.reply("You can't buy negative packs").setEphemeral(true).queue();
            return;
        }
        if(event.getOption("collection") != null && collectionDoesntExists(event.getOption("collection").getAsString())){ // need collection to exist
            event.reply("That collection does not exist").setEphemeral(true).queue();
            return;
        }
        PlayerCardData player = playerCardDataRepository.findById(event.getUser().getIdLong()).get();
        int count = event.getOption("count").getAsInt();
        if(event.getOption("collection") == null) { // regular pack
            int purchaseTotal = count * 100;
            if(!player.hasCurrency(purchaseTotal)){
                event.reply("You do not have " + purchaseTotal + " pips").setEphemeral(true).queue();
                return;
            }
            player.removeCurrency(purchaseTotal);
            player.addPack(count);
            playerCardDataRepository.save(player);
            event.reply("You have purchased " + count + " generic packs").queue();
        } else { // collection pack
            int purchaseTotal = count * 250;
            if(!player.hasCurrency(purchaseTotal)){
                event.reply("You do not have " + purchaseTotal + " pips").setEphemeral(true).queue();
                return;
            }
            String collection = event.getOption("collection").getAsString();
            player.removeCurrency(purchaseTotal);
            player.addPack(collection, count);
            playerCardDataRepository.save(player);
            event.reply("You have purchased " + count + " " + collection + " packs").queue();
        }
    }

    @DiscordMapping(Id = "cards", SubId = "collection")
    private void collectionSlashCommand(SlashCommandInteractionEvent event){
        event.deferReply().queue();
        long userId = event.getOption("user") != null ? event.getOption("user").getAsUser().getIdLong() : event.getUser().getIdLong();
        String username = event.getOption("user") != null ? event.getOption("user").getAsUser().getName() : event.getUser().getName();
        User user = event.getOption("user") != null ? event.getOption("user").getAsUser() : event.getUser();
        PlayerCardData player = playerCardDataRepository.findById(userId).get();
        OptionMapping collection = event.getOption("collection");
        if(collection != null){
            if(collectionDoesntExists(collection.getAsString())) {
                event.getHook().sendMessage("That collection does not exist").queue();
                return;
            }
            LinkedList<CardData> cardsInCollection = cardDataRepository.findCardsByCollection(collection.getAsString());
            if(cardsInCollection.size() > PAGE_SIZE) {
                event.getHook().sendMessageEmbeds(EmbedMessageGenerator.specificCollectionView(user, collection.getAsString(), new LinkedList<>(player.getDeck()), cardsInCollection, 0))
                        .addActionRow(Button.primary("cards_next_page", "Next page")).queue();
            } else {
                event.getHook().sendMessageEmbeds(EmbedMessageGenerator.specificCollectionView(user, collection.getAsString(), new LinkedList<>(player.getDeck()), cardsInCollection, 0)).queue();
            }
        } else {
            event.getHook().sendMessageEmbeds(EmbedMessageGenerator.overallCollectionView(username, new LinkedList<>(player.getDeck()), cardDataRepository)).queue();
        }
    }

    @DiscordMapping(Id = "cards_next_page")
    private void nextPage(ButtonInteractionEvent event){
        String title = event.getMessage().getEmbeds().get(0).getTitle();
        String username = title.split(" ")[0].replace("'s", "");
        User user = event.getGuild().getMemberById(event.getMessage().getEmbeds().get(0).getDescription().split("\n")[0].replace("<@", "").replace(">", "")).getUser();
        if(event.getUser().getIdLong() != user.getIdLong()){
            event.reply("Only the person who did the slash command is allowed to do this").setEphemeral(true).queue();
            return;
        }
        PlayerCardData player = playerCardDataRepository.findById(user.getIdLong()).get();
        String collection = title.replace(username + "'s", "").replace("collection", "").trim();
        int page = Integer.parseInt(event.getMessage().getEmbeds().get(0).getFooter().getText().split(" ")[1]) - 1;
        LinkedList<CardData> cardsInCollection = cardDataRepository.findCardsByCollection(collection);
        page = (page + 1) * PAGE_SIZE > cardsInCollection.size() ? 0: page + 1;
        event.getMessage().editMessageEmbeds(EmbedMessageGenerator.specificCollectionView(user, collection, new LinkedList<>(player.getDeck()), cardsInCollection, page)).queue();
        event.deferEdit().queue();

    }

    @DiscordMapping
    public void guildVoidUpdate(GuildVoiceUpdateEvent event) {
        try {
            if (event.getChannelLeft() != null) {
                PlayerCardData pcd = playerCardDataRepository.findById(event.getMember().getIdLong()).get();
                long seconds = (new Date().getTime() - pcd.getJoinedVoice().getTime()) / 1000;
                pcd.addProgress(seconds);
                pcd.setJoinedVoice(null);
                playerCardDataRepository.save(pcd);
            }
            if (event.getChannelJoined() != null) {
                if (event.getGuild().getAfkChannel() == null ||
                        event.getChannelJoined().getIdLong() != event.getGuild().getAfkChannel().getIdLong()) {
                    PlayerCardData pcd = playerCardDataRepository.findById(event.getMember().getIdLong()).get();
                    pcd.setJoinedVoice(new Date());
                    playerCardDataRepository.save(pcd);
                }
            }
        } catch(NullPointerException ignored){

        }
    }

    @DiscordMapping
    public void genericMessageReaction(GenericMessageReactionEvent event) {
        if(!event.isFromGuild()) return;
        if(event.getUser().isBot()) return;
        PlayerCardData pcd = playerCardDataRepository.findById(event.getMember().getIdLong()).get();
        if(event.getRawData().getString("t").equals("MESSAGE_REACTION_ADD")){
            pcd.addProgress(270);
        } else {
            pcd.removeProgress(270);
        }
        playerCardDataRepository.save(pcd);
    }

    @DiscordMapping
    public void messageReceived(MessageReceivedEvent event) {
        if(!event.isFromGuild()) return;
        if(event.getAuthor().isBot()) return;
        PlayerCardData pcd = playerCardDataRepository.findById(event.getAuthor().getIdLong()).get();
        pcd.addProgress(180);
        playerCardDataRepository.save(pcd);
    }

    @PostMapping("/api/cards")
    private void addCards(@RequestBody String value) throws JSONException {
        JSONObject json = new JSONObject(value);
        if(!json.has("token")) return;
        if(!json.getString("token").equals(apiToken)) return;
        int index = json.getInt("id start");
        String collection = json.getString("collection");
        LinkedList<CardData> newCards = new LinkedList<>();
        JSONArray jsonCards = json.getJSONArray("cards");
        for(int i = 0; i < jsonCards.length(); i++){
            JSONObject card = jsonCards.getJSONObject(i);
            CardData newCard = new CardData();
            newCard.setId(index + i);
            newCard.setName(card.getString("name"));
            newCard.setCollection(collection);
            if(card.has("rarity")){
                newCard.setRarity(card.getInt("rarity"));
            } else {
                newCard.setRarity(new Random().nextInt(10) + 1);
            }
            newCards.add(newCard);
        }
        cardDataRepository.saveAll(newCards);
    }

    @Scheduled(cron = "0 */5 * * * *")
    private void fiveMinuteTask() {
        fiveMinuteTasks();
    }

    private boolean collectionDoesntExists(String collection){
        return !cardDataRepository.listCardCollections().contains(collection);
    }

    private void fiveMinuteTasks(){
        if(bot != null){
            for(Guild guild: bot.getGuilds()){
                for(VoiceChannel channel: guild.getVoiceChannels()){
                    if(guild.getAfkChannel() != null || (guild.getAfkChannel() != null && guild.getAfkChannel().getIdLong() != channel.getIdLong())){
                        for(Member member: channel.getMembers()){
                            PlayerCardData player = playerCardDataRepository.findById(member.getIdLong()).get();
                            if(player.getJoinedVoice() == null){
                                player.setJoinedVoice(new Date());
                                playerCardDataRepository.save(player);
                            }
                        }
                    }
                }
            }
        }
        LinkedList<PlayerCardData> updated = new LinkedList<>();
        for(PlayerCardData player: playerCardDataRepository.findAll()){
            boolean edited = false;
            if(player.getJoinedVoice() != null){
                long seconds = (new Date().getTime() - player.getJoinedVoice().getTime()) / 1000;
                player.setJoinedVoice(new Date());
                player.addProgress(seconds);
                edited = true;
            }
            while(player.getProgress() >= 3600){
                player.removeProgress(3600);
                player.addPack();
                edited = true;
            }
            if(edited) updated.add(player);
        }
        playerCardDataRepository.saveAll(updated);
    }
}
