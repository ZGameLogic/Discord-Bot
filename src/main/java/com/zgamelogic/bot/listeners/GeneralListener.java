package com.zgamelogic.bot.listeners;

import com.zgamelogic.bot.utils.EmbedMessageGenerator;
import com.zgamelogic.discord.annotations.DiscordController;
import com.zgamelogic.data.database.guildData.GuildDataRepository;
import com.zgamelogic.data.intermediates.messaging.Message;
import com.zgamelogic.discord.annotations.mappings.ButtonMapping;
import com.zgamelogic.discord.annotations.mappings.GenericDiscordMapping;
import com.zgamelogic.discord.annotations.mappings.ModalMapping;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.modals.Modal;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.UnavailableGuildLeaveEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@DiscordController
@RestController
@Slf4j
public class GeneralListener {

    private final GuildDataRepository guildData;

    private JDA bot;

    @Value("${api.token}")
    private String apiToken;

    @Autowired
    public GeneralListener(GuildDataRepository guildData){
        this.guildData = guildData;
    }

    @GenericDiscordMapping(event = ReadyEvent.class)
    private void onReady(ReadyEvent event){
        bot = event.getJDA();
    }

    @GenericDiscordMapping(event = MessageReceivedEvent.class)
    public void onMessageReceived(MessageReceivedEvent event) {
        if(event.isFromGuild()) return;
        if(event.getAuthor().isBot()) return;
        if(event.getMessage().getContentRaw().charAt(0) == '!') return;
        event.getJDA().getUserById(232675572772372481L).openPrivateChannel().queue(channel -> channel.sendMessage("Message from " + event.getAuthor().getName() + ":" + event.getAuthor().getId() + "\n" + event.getMessage().getContentRaw())
                .setComponents(ActionRow.of(Button.secondary("reply_message", "Reply"))).queue());
    }

    @ButtonMapping(id = "reply_message")
    private void replyMessageButtonPresses(ButtonInteractionEvent event){
        TextInput message = TextInput.create("message", "Reply", TextInputStyle.PARAGRAPH).build();
        event.replyModal(Modal.create("reply_message_modal", "Message response").addActionRow(message).build()).queue();
    }

    @ModalMapping(id = "reply_message_modal")
    private void modalResponseMessageReply(ModalInteractionEvent event){
        event.getJDA().getUserById(
                event.getMessage().getContentRaw().split("\n")[0].split(":")[1]
        ).openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(event.getValue("message").getAsString()).queue());
        event.reply("Message sent back\n" + event.getValue("message").getAsString()).queue();
    }

    @ButtonMapping(id = "reply_text")
    private void replyTextMessageButtonPress(ButtonInteractionEvent event){
        TextInput message = TextInput.create("message", "Reply", TextInputStyle.PARAGRAPH).build();
        event.replyModal(Modal.create("reply_text_modal", "Message response").addActionRow(message).build()).queue();
    }

    @DiscordMapping
    public void onUnavailableGuildLeave(UnavailableGuildLeaveEvent event) {
        guildData.deleteById(event.getGuildIdLong());
    }

    @PostMapping("/message")
    private ResponseEntity<?> sendMessage(@RequestBody Message message){
        try {
            log.info(message.toString());
            log.info(bot.getGuildById(message.getGuildId()).getName());
            bot.getGuildById(message.getGuildId())
                    .getTextChannelById(message.getChannelId()).sendMessage(
                            EmbedMessageGenerator.message(message)
                    ).queue();
            return ResponseEntity.status(200).build();
        } catch (Exception ignored){
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/api/message")
    private String postMessage(@RequestBody String value) throws JSONException {
        JSONObject json = new JSONObject(value);
        if(!json.has("token")) return "Invalid token";
        if(!json.getString("token").equals(apiToken)) return "Invalid token";
        boolean toGuild = json.getBoolean("to guild");
        if(toGuild){
            String guildId = json.getString("guild id");
            String channelId = json.getString("channel id");
            String message = json.getString("message");
            Guild guild = bot.getGuildById(guildId);
            TextChannel channel = guild.getTextChannelById(channelId);
            channel.sendMessage(message).queue();
            return "Message sent to " + guild.getName() + "/" + channel.getName();
        } else {
            String userId = json.getString("user id");
            String message = json.getString("message");
            User user = bot.getUserById(userId);
            PrivateChannel channel = user.openPrivateChannel().complete();
            channel.sendMessage(message).queue();
            return "Message sent to " + user.getName();
        }
    }

    @GetMapping("health")
    private String healthCheck(){
        return "Healthy";
    }
}
