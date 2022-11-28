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
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
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
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CardBot extends AdvancedListenerAdapter {

    private final CardDataRepository cardDataRepository;
    private final GuildCardDataRepository guildCardDataRepository;
    private final PlayerCardDataRepository playerCardDataRepository;
    private final GuildDataRepository guildDataRepository;

    public CardBot(GuildDataRepository guildDataRepository, CardDataRepository cardDataRepository, GuildCardDataRepository guildCardDataRepository, PlayerCardDataRepository playerCardDataRepository) {
        this.cardDataRepository = cardDataRepository;
        this.guildCardDataRepository = guildCardDataRepository;
        this.playerCardDataRepository = playerCardDataRepository;
        this.guildDataRepository = guildDataRepository;
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
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
                                  .addOption(OptionType.STRING,"collection", "Specific collection", true, true)
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
                                new LinkedList<>(Collections.singletonList(Permission.VIEW_CHANNEL)),
                                new LinkedList<>(Collections.singletonList(Permission.MESSAGE_SEND))
                        )
                .complete().getIdLong());
        guildCardDataRepository.save(data);
        guildDataRepository.save(guildDataRepository.getOne(event.getGuild().getIdLong()).setCardsEnabled(true));
        ActionRow row = event.getMessage().getActionRows().get(0);
        row.updateComponent("enable_cards", Button.success("disable_cards", "Cards bot"));
        event.getHook().editOriginalComponents(row).queue();
    }

    @ButtonResponse("disable_cards")
    private void disableCards(ButtonInteractionEvent event){
        event.editButton(Button.danger("enable_cards", "Cards bot")).queue();
        guildDataRepository.save(guildDataRepository.getOne(event.getGuild().getIdLong()).setCardsEnabled(false));
        GuildCardData guildData = guildCardDataRepository.getOne(event.getGuild().getIdLong());
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
        String[] words = cardDataRepository.listCardCollectionsById(
                playerCardDataRepository.getOne(event.getUser().getIdLong()).getDeck()
        ).toArray(new String[0]);
        List<Command.Choice> options = Stream.of(words)
                .filter(word -> word.startsWith(event.getFocusedOption().getValue()))
                .map(word -> new Command.Choice(word, word))
                .collect(Collectors.toList());
        event.replyChoices(options).queue();
    }

    @AutoCompleteResponse(slashCommandId = "cards", slashSubCommandId = "sell", focusedOption = "name")
    private void sellNameAutocomplete(CommandAutoCompleteInteractionEvent event){
        String collection = event.getOption("collection") == null ? "" : event.getOption("collection").getAsString();
        Set<String> names = new HashSet<>(cardDataRepository.findByCollectionAndIds(
                playerCardDataRepository.getOne(event.getUser().getIdLong()).getDeck(),
                collection
        ));
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
        Guild guild = event.getGuild();
        TextChannel shopChannel = guild.getTextChannelById(guildCardDataRepository.getOne(guild.getIdLong()).getShopTextChannelId());
        long user = event.getUser().getIdLong();
        LinkedList<CardData> results = cardDataRepository.findCardsByCollectionAndName(
                event.getOption("collection").getAsString(),
                event.getOption("name").getAsString()
        );
        if(results.size() == 0){
            event.getHook().setEphemeral(true).sendMessage("That card does not exist").queue();
            return;
        }
        CardData card = results.get(0);
        if(event.getOption("price").getAsInt() <= 0){
            event.getHook().setEphemeral(true).sendMessage("Price must be positive").queue();
            return;
        }
        PlayerCardData player = playerCardDataRepository.getOne(event.getUser().getIdLong());
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

    @ButtonResponse("purchase_card")
    private void purchaseCard(ButtonInteractionEvent event){
        long cardId = Long.parseLong(event.getMessage().getEmbeds().get(0).getDescription().split("__")[1]);
        long userId = Long.parseLong(event.getMessage().getEmbeds().get(0).getFooter().getText());
        int price = Integer.parseInt(event.getMessage().getEmbeds().get(0).getFields().get(0).getValue());
        PlayerCardData player = playerCardDataRepository.getOne(event.getUser().getIdLong());
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
        PlayerCardData seller = playerCardDataRepository.getOne(userId);
        seller.addCurrency(price);
        playerCardDataRepository.save(seller);
    }

    @SlashResponse(value = "cards", subCommandName = "open_packs")
    private void openPacks(SlashCommandInteractionEvent event){
        event.deferReply().queue();
        PlayerCardData player = playerCardDataRepository.getOne(event.getUser().getIdLong());
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
            moneyMade += cardDataRepository.getOne(id).getSellback();
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
                EmbedMessageGenerator.cardPlayerStatus(event.getUser().getName(), playerCardDataRepository.getOne(event.getUser().getIdLong()))
        ).queue();
    }

    @SlashResponse(value = "cards", subCommandName = "buy_pack")
    private void buyPackSlashCommand(SlashCommandInteractionEvent event){
        if(event.getOption("count").getAsInt() <= 0) { // need a positive number of packs
            event.reply("You can't buy negative packs").setEphemeral(true).queue();
            return;
        }
        if(event.getOption("collection") != null && collectionDoesntExists(event.getOption("collection").getAsString())){ // need collection to exist
            event.reply("That collection does not exist").setEphemeral(true).queue();
            return;
        }
        PlayerCardData player = playerCardDataRepository.getOne(event.getUser().getIdLong());
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

    @SlashResponse(value = "cards", subCommandName = "collection")
    private void collectionSlashCommand(SlashCommandInteractionEvent event){
        event.deferReply().queue();
        long userId = event.getOption("user") != null ? event.getOption("user").getAsUser().getIdLong() : event.getUser().getIdLong();
        String username = event.getOption("user") != null ? event.getOption("user").getAsUser().getName() : event.getUser().getName();
        PlayerCardData player = playerCardDataRepository.getOne(userId);
        OptionMapping collection = event.getOption("collection");
        if(collection != null){
            if(collectionDoesntExists(collection.getAsString())) {
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
            PlayerCardData pcd = playerCardDataRepository.getOne(event.getMember().getIdLong());
            long seconds = (new Date().getTime() - pcd.getJoinedVoice().getTime()) / 1000;
            pcd.addProgress(seconds);
            pcd.setJoinedVoice(null);
            playerCardDataRepository.save(pcd);
        }
        if(event.getChannelJoined() != null){
            if(event.getGuild().getAfkChannel() == null ||
                    event.getChannelJoined().getIdLong() != event.getGuild().getAfkChannel().getIdLong()){
                PlayerCardData pcd = playerCardDataRepository.getOne(event.getMember().getIdLong());
                pcd.setJoinedVoice(new Date());
                playerCardDataRepository.save(pcd);
            }
        }
    }

    @Override
    public void onGenericMessageReaction(GenericMessageReactionEvent event) {
        if(!event.isFromGuild()) return;
        if(event.getUser().isBot()) return;
        PlayerCardData pcd = playerCardDataRepository.getOne(event.getMember().getIdLong());
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
        PlayerCardData pcd = playerCardDataRepository.getOne(event.getAuthor().getIdLong());
        pcd.addProgress(180);
        playerCardDataRepository.save(pcd);
    }

    private boolean collectionDoesntExists(String collection){
        return !cardDataRepository.listCardCollections().contains(collection);
    }

    public void tenMinuteTasks(){
        LinkedList<PlayerCardData> updated = new LinkedList<>();
        for(PlayerCardData player: playerCardDataRepository.findAll()){
            boolean edited = false;
            if(player.getJoinedVoice() != null){
                long seconds = (new Date().getTime() - player.getJoinedVoice().getTime()) / 1000;
                player.setJoinedVoice(new Date());
                player.addProgress(seconds);
                edited = true;
            }
            if(player.getProgress() >= 3600){
                player.setProgress(0l);
                player.addPack();
                edited = true;
            }
            if(edited) updated.add(player);
        }
        playerCardDataRepository.saveAll(updated);
    }
}