package bot.listeners;

import com.zgamelogic.AdvancedListenerAdapter;
import data.database.cardData.cards.CardData;
import data.database.cardData.cards.CardDataRepository;
import data.database.cardData.guild.GuildCardData;
import data.database.cardData.guild.GuildCardDataRepository;
import data.database.cardData.player.PlayerCardDataRepository;
import data.database.guildData.GuildData;
import data.database.guildData.GuildDataRepository;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.LinkedList;

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
            // TODO update points (if the join time isnt null)
        }
        if(event.getChannelJoined() != null){
            // TODO update player joined timestamp (or keep null if in afk)
        }
    }

    @Override
    public void onGenericMessageReaction(GenericMessageReactionEvent event) {
        // TODO update points
    }

    @Override
    public void onGenericMessage(GenericMessageEvent event) {
        if(event.isFromGuild()){
            // TODO update points
        }
    }
}
