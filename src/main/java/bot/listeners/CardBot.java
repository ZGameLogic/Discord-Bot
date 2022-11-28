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
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

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
                                  .addOption(OptionType.STRING, "collection", "Specific collection data", false, true),
                          new SubcommandData("sell", "Sell a specific card you own")
                                  .addOption(OptionType.STRING,"collection", "Specific collection", true)
                                  .addOption(OptionType.STRING, "name", "Specific card name", true, true)
                                  .addOption(OptionType.INTEGER, "price", "Price for card", true)
                  )
          ).complete().getIdLong()
        );
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

    @SlashResponse(value = "cards", subCommandName = "collection")
    private void collectionSlashCommand(SlashCommandInteractionEvent event){
        event.deferReply().queue();
        PlayerCardData player = playerCardDataRepository.getById(event.getUser().getIdLong());
        OptionMapping collection = event.getOption("collection");
        if(collection != null){
            if(!collectionExists(collection.getAsString())) {
                event.getHook().sendMessage("That collection does not exist").queue();
                return;
            }
            event.getHook().sendMessageEmbeds(EmbedMessageGenerator.specificCollectionView(event.getUser().getName(), collection.getAsString(), new LinkedList<>(player.getDeck()), cardDataRepository)).queue();
        } else {
            event.getHook().sendMessageEmbeds(EmbedMessageGenerator.overallCollectionView(event.getUser().getName(), new LinkedList<>(player.getDeck()), cardDataRepository)).queue();
        }
    }

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        if(event.getChannelLeft() != null){
            PlayerCardData pcd = playerCardDataRepository.getById(event.getMember().getIdLong());
            long seconds = (new Date().getTime() - pcd.getJoinedVoice().getTime()) / 1000;
            pcd.addProgress(seconds);
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
