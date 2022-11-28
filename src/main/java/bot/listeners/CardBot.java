package bot.listeners;

import bot.utils.EmbedMessageGenerator;
import com.zgamelogic.AdvancedListenerAdapter;
import data.database.cardData.cards.CardData;
import data.database.cardData.cards.CardDataRepository;
import data.database.cardData.guild.GuildCardData;
import data.database.cardData.guild.GuildCardDataRepository;
import data.database.cardData.player.PlayerCardData;
import data.database.cardData.player.PlayerCardDataRepository;
import data.database.guildData.GuildDataRepository;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
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
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CardBot extends AdvancedListenerAdapter {

    private CardDataRepository cardDataRepository;
    private GuildCardDataRepository guildCardDataRepository;
    private PlayerCardDataRepository playerCardDataRepository;
    private GuildDataRepository guildDataRepository;

    public CardBot(GuildDataRepository guildDataRepository, CardDataRepository cardDataRepository, GuildCardDataRepository guildCardDataRepository, PlayerCardDataRepository playerCardDataRepository) {
        this.cardDataRepository = cardDataRepository;
        this.guildCardDataRepository = guildCardDataRepository;
        this.playerCardDataRepository = playerCardDataRepository;
        this.guildDataRepository = guildDataRepository;
    }

    @Override
    public void onReady(ReadyEvent event) {
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

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        playerCardDataRepository.save(new PlayerCardData(event.getUser().getIdLong()));
    }

    @ButtonResponse("enable_cards")
    private void enableCards(ButtonInteractionEvent event){
        event.deferEdit().queue();
        GuildCardData data = new GuildCardData();
        Guild guild = event.getGuild();
        data.setId(guild.getIdLong());
        data.setSlashCommandId(
          guild.upsertCommand(Commands.slash("cards", "Slash command for card bot")
                  .addSubcommands(
                          new SubcommandData("collection", "View your entire collection completion")
                                  .addOption(OptionType.STRING, "collection", "Specific collection data", false, true)
                                  .addOption(OptionType.USER, "user", "Specific user data", false),
                          new SubcommandData("sell", "Sell a specific card you own")
                                  .addOption(OptionType.STRING,"collection", "Specific collection", true)
                                  .addOption(OptionType.STRING, "name", "Specific card name", true, true)
                                  .addOption(OptionType.INTEGER, "price", "Price for card", true),
                          new SubcommandData("buy_pack", "Buy a number of packs. 100 pips each.")
                                  .addOption(OptionType.INTEGER, "count", "Number of packs you want to buy", true)
                                  .addOption(OptionType.STRING, "collection", "Specific collection of cards. 250 pips each.", false, true),
                          new SubcommandData("status", "View how many unopened packs you have and your pip count"),
                          new SubcommandData("open_packs", "Opens all the packs that you have")
                  )
          ).complete().getIdLong()
        );
        data.setShopTextChannelId(
                guild
                        .createTextChannel("card shop")
                        .setTopic("A shop for all things cards")
                        .addRolePermissionOverride(
                                event.getGuild().getPublicRole().getIdLong(),
                                new LinkedList<>(Arrays.asList(Permission.VIEW_CHANNEL)),
                                new LinkedList<>(Arrays.asList(Permission.MESSAGE_SEND))
                        )
                .complete().getIdLong());
        guildCardDataRepository.save(data);
        guildDataRepository.save(guildDataRepository.getById(event.getGuild().getIdLong()).setCardsEnabled(true));
        ActionRow row = event.getMessage().getActionRows().get(0);
        row.updateComponent("enable_cards", Button.success("disable_cards", "Cards bot"));
        event.getHook().editOriginalComponents(row).queue();
    }

    @ButtonResponse("disable_cards")
    private void disableCards(ButtonInteractionEvent event){
        event.editButton(Button.danger("enable_cards", "Cards bot")).queue();
        guildDataRepository.save(guildDataRepository.getById(event.getGuild().getIdLong()).setCardsEnabled(false));
        GuildCardData guildData = guildCardDataRepository.getById(event.getGuild().getIdLong());
        event.getGuild().retrieveCommandById(guildData.getSlashCommandId()).queue(command -> command.delete().queue());
        event.getGuild().getTextChannelById(guildData.getShopTextChannelId()).delete().queue();
        guildCardDataRepository.delete(guildData);
    }

    @AutoCompleteResponse(slashCommandId = "cards", slashSubCommandId = "collection", focusedOption = "collection")
    private void collectionAutocomplete(CommandAutoCompleteInteractionEvent event){
        String[] words = cardDataRepository.listCardCollections().toArray(new String[0]);
        List<Command.Choice> options = Stream.of(words)
                .filter(word -> word.startsWith(event.getFocusedOption().getValue()))
                .map(word -> new Command.Choice(word, word))
                .collect(Collectors.toList());
        event.replyChoices(options).queue();
    }

    @AutoCompleteResponse(slashCommandId = "cards", slashSubCommandId = "buy_pack", focusedOption = "collection")
    private void collectionBuyAutocomplete(CommandAutoCompleteInteractionEvent event){
        String[] words = cardDataRepository.listCardCollections().toArray(new String[0]);
        List<Command.Choice> options = Stream.of(words)
                .filter(word -> word.startsWith(event.getFocusedOption().getValue()))
                .map(word -> new Command.Choice(word, word))
                .collect(Collectors.toList());
        event.replyChoices(options).queue();
    }

    @AutoCompleteResponse(slashCommandId = "cards", slashSubCommandId = "sell", focusedOption = "collection")
    private void sellCollectionAutocomplete(CommandAutoCompleteInteractionEvent event){
        Set<String> collections = new HashSet<>();
        for(long cardId: playerCardDataRepository.getById(event.getUser().getIdLong()).getDeck()){
            collections.add(cardDataRepository.getById(cardId).getCollection());
        }
        String[] words = collections.toArray(new String[0]);
        List<Command.Choice> options = Stream.of(words)
                .filter(word -> word.startsWith(event.getFocusedOption().getValue()))
                .map(word -> new Command.Choice(word, word))
                .collect(Collectors.toList());
        event.replyChoices(options).queue();
    }

    @AutoCompleteResponse(slashCommandId = "cards", slashSubCommandId = "sell", focusedOption = "name")
    private void sellNameAutocomplete(CommandAutoCompleteInteractionEvent event){
        Set<String> names = new HashSet<>();
        String collection = event.getOption("collection").getAsString();
        for(long cardId: playerCardDataRepository.getById(event.getUser().getIdLong()).getDeck()){
            CardData card = cardDataRepository.getById(cardId);
            if(card.getCollection().equals(collection)) names.add(cardDataRepository.getById(cardId).getCollection());
        }
        String[] words = names.toArray(new String[0]);
        List<Command.Choice> options = Stream.of(words)
                .filter(word -> word.startsWith(event.getFocusedOption().getValue()))
                .map(word -> new Command.Choice(word, word))
                .collect(Collectors.toList());
        event.replyChoices(options).queue();
    }

    @SlashResponse(value = "cards", subCommandName = "sell")
    private void sellSlashCommand(SlashCommandInteractionEvent event){
        event.deferReply().queue();

    }

    @SlashResponse(value = "cards", subCommandName = "open_packs")
    private void openPacks(SlashCommandInteractionEvent event){
        event.deferReply().queue();
        PlayerCardData player = playerCardDataRepository.getById(event.getUser().getIdLong());
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
        newCards.forEach(id -> player.addCard(id));
        int moneyMade = 0;
        for(long id: dupCards){
            moneyMade += cardDataRepository.getById(id).getSellback();
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

    @SlashResponse(value = "cards", subCommandName = "status")
    private void statusSlashCommand(SlashCommandInteractionEvent event){
        event.replyEmbeds(
                EmbedMessageGenerator.cardPlayerStatus(event.getUser().getName(), playerCardDataRepository.getById(event.getUser().getIdLong()))
        ).queue();
    }

    @SlashResponse(value = "cards", subCommandName = "buy_pack")
    private void buyPackSlashCommand(SlashCommandInteractionEvent event){
        if(event.getOption("count").getAsInt() <= 0) { // need a positive number of packs
            event.reply("You can't buy negative packs").setEphemeral(true).queue();
            return;
        }
        if(event.getOption("collection") != null && !collectionExists(event.getOption("collection").getAsString())){ // need collection to exist
            event.reply("That collection does not exist").setEphemeral(true).queue();
            return;
        }
        PlayerCardData player = playerCardDataRepository.getById(event.getUser().getIdLong());
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
        System.out.println(playerCardDataRepository.getById(event.getUser().getIdLong()).getPacks().size());
    }

    @SlashResponse(value = "cards", subCommandName = "collection")
    private void collectionSlashCommand(SlashCommandInteractionEvent event){
        event.deferReply().queue();
        long userId = event.getOption("user") != null ? event.getOption("user").getAsUser().getIdLong() : event.getUser().getIdLong();
        String username = event.getOption("user") != null ? event.getOption("user").getAsUser().getName() : event.getUser().getName();
        PlayerCardData player = playerCardDataRepository.getById(userId);
        OptionMapping collection = event.getOption("collection");
        if(collection != null){
            if(!collectionExists(collection.getAsString())) {
                event.getHook().sendMessage("That collection does not exist").queue();
                return;
            }
            event.getHook().sendMessageEmbeds(EmbedMessageGenerator.specificCollectionView(username, collection.getAsString(), new LinkedList<>(player.getDeck()), cardDataRepository)).queue();
        } else {
            event.getHook().sendMessageEmbeds(EmbedMessageGenerator.overallCollectionView(username, new LinkedList<>(player.getDeck()), cardDataRepository)).queue();
        }
    }

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        if(event.getChannelLeft() != null){
            PlayerCardData pcd = playerCardDataRepository.getById(event.getMember().getIdLong());
            long seconds = (new Date().getTime() - pcd.getJoinedVoice().getTime()) / 1000;
            pcd.addProgress(seconds);
            pcd.setJoinedVoice(null);
            playerCardDataRepository.save(pcd);
        }
        if(event.getChannelJoined() != null){
            if(event.getGuild().getAfkChannel() == null ||
                    event.getChannelJoined().getIdLong() != event.getGuild().getAfkChannel().getIdLong()){
                PlayerCardData pcd = playerCardDataRepository.getById(event.getMember().getIdLong());
                pcd.setJoinedVoice(new Date());
                playerCardDataRepository.save(pcd);
            }
        }
    }

    @Override
    public void onGenericMessageReaction(GenericMessageReactionEvent event) {
        if(!event.isFromGuild()) return;
        if(event.getUser().isBot()) return;
        PlayerCardData pcd = playerCardDataRepository.getById(event.getMember().getIdLong());
        if(event.getRawData().getString("t").equals("MESSAGE_REACTION_ADD")){
            pcd.addProgress(270);
        } else {
            pcd.removeProgress(270);
        }
        playerCardDataRepository.save(pcd);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if(!event.isFromGuild()) return;
        if(event.getAuthor().isBot()) return;
        PlayerCardData pcd = playerCardDataRepository.getById(event.getAuthor().getIdLong());
        pcd.addProgress(180);
        playerCardDataRepository.save(pcd);
    }

    private boolean collectionExists(String collection){
        return cardDataRepository.listCardCollections().contains(collection);
    }
}
