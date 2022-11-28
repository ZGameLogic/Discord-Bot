package bot.listeners;

import com.zgamelogic.AdvancedListenerAdapter;
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
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

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
        guildCardDataRepository.delete(guildData);
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
}
