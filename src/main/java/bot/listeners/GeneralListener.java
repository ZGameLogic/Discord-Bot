package bot.listeners;

import bot.utils.EmbedMessageGenerator;
import com.zgamelogic.annotations.Bot;
import com.zgamelogic.annotations.DiscordController;
import com.zgamelogic.annotations.DiscordMapping;
import data.database.guildData.GuildData;
import data.database.guildData.GuildDataRepository;
import data.intermediates.messaging.Message;
import services.TwilioService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.UnavailableGuildLeaveEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Collections;
import java.util.LinkedList;

@DiscordController
@Slf4j
public class GeneralListener {

    private final GuildDataRepository guildData;
    private final TwilioService twilioService;

    @Bot
    private JDA bot;

    @Value("${api.token}")
    private String apiToken;

    @Autowired
    public GeneralListener(GuildDataRepository guildData, TwilioService twilioService){
        this.guildData = guildData;
        this.twilioService = twilioService;
    }

    @DiscordMapping
    public void ready(@NotNull ReadyEvent event) {
        new Thread(()-> {
            //update guilds
            for(Guild guild : event.getJDA().getGuilds()){
                if(!guildData.existsById(guild.getIdLong())){
                    welcomeBot(guild);
                }
            }
            for(GuildData guild: guildData.findAll()){
                Guild g = event.getJDA().getGuildById(guild.getId());
                g.getTextChannelById(guild.getConfigChannelId()).retrieveMessageById(guild.getConfigMessageId()).queue(message -> {
                    LinkedList<LayoutComponent> components = new LinkedList<>();
                    LinkedList<ItemComponent> ics = new LinkedList<>();
                    if(guild.getChatroomEnabled() == null || !guild.getChatroomEnabled()){
                        ics.add(Button.danger("enable_party", "Party Bot"));
                    } else {
                        ics.add(Button.success("disable_party", "Party Bot"));
                    }
                    if(guild.getPlanEnabled() == null || !guild.getPlanEnabled()){
                        ics.add(Button.danger("enable_plan", "Plan bot"));
                    } else {
                        ics.add(Button.success("disable_plan", "Plan bot"));
                    }
                    if(guild.getCardsEnabled() == null || !guild.getCardsEnabled()){
                        ics.add(Button.danger("enable_cards", "Cards bot"));
                    } else {
                        ics.add(Button.success("disable_cards", "Cards bot"));
                    }
                    if(guild.getHuntEnabled() == null || !guild.getHuntEnabled()){
                        ics.add(Button.danger("enable_hunt", "Hunt bot"));
                    } else {
                        ics.add(Button.success("disable_hunt", "Hunt bot"));
                    }
                    if(guild.getGeneratorEnabled() == null || !guild.getGeneratorEnabled()){
                        ics.add(Button.danger("enable_generator", "Generator bot"));
                    } else {
                        ics.add(Button.success("disable_generator", "Generator bot"));
                    }
                    ActionRow row = ActionRow.of(ics);
                    components.add(row);
                    message.editMessageComponents(components).queue();
                });
            }
        }, "Guild Checking Thread").start();
    }

    @DiscordMapping
    public void onGuildJoin(GuildJoinEvent event) {
        welcomeBot(event.getGuild());
    }

    @DiscordMapping
    public void onGuildLeave(GuildLeaveEvent event) {
        Guild guild = event.getGuild();
        try {
            // edit the discord
            GuildData savedGuild = guildData.findById(event.getGuild().getIdLong()).get();
            if(savedGuild.getChatroomEnabled()) {
                // commands
                guild.deleteCommandById(savedGuild.getLimitCommandId()).queue();
                guild.deleteCommandById(savedGuild.getRenameCommandId()).queue();
                // voice channel shenanigans
                guild.getVoiceChannelById(savedGuild.getCreateChatId()).delete().queue();
                guild.getCategoryById(savedGuild.getPartyCategory()).delete().queue();
            }
            guild.getTextChannelById(savedGuild.getConfigChannelId()).delete().queue();
        } catch (Exception e){
            log.warn("Unable to delete guild data. Was removed too quickly.");
        }
        guildData.deleteById(event.getGuild().getIdLong());
    }

    @DiscordMapping
    public void onMessageReceived(MessageReceivedEvent event) {
        if(event.isFromGuild()) return;
        if(event.getAuthor().isBot()) return;
        if(event.getMessage().getContentRaw().charAt(0) == '!') return;
        event.getJDA().getUserById(232675572772372481L).openPrivateChannel().queue(channel -> channel.sendMessage("Message from " + event.getAuthor().getName() + ":" + event.getAuthor().getId() + "\n" + event.getMessage().getContentRaw())
                .setActionRow(Button.secondary("reply_message", "Reply")).queue());
    }

    @DiscordMapping(Id = "reply_message")
    private void replyMessageButtonPresses(ButtonInteractionEvent event){
        TextInput message = TextInput.create("message", "Reply", TextInputStyle.PARAGRAPH).build();
        event.replyModal(Modal.create("reply_message_modal", "Message response").addActionRow(message).build()).queue();
    }

    @DiscordMapping(Id = "reply_message_modal")
    private void modalResponseMessageReply(ModalInteractionEvent event){
        event.getJDA().getUserById(
                event.getMessage().getContentRaw().split("\n")[0].split(":")[1]
        ).openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(event.getValue("message").getAsString()).queue());
        event.reply("Message sent back\n" + event.getValue("message").getAsString()).queue();
    }

    @DiscordMapping(Id = "reply_text")
    private void replyTextMessageButtonPress(ButtonInteractionEvent event){
        TextInput message = TextInput.create("message", "Reply", TextInputStyle.PARAGRAPH).build();
        event.replyModal(Modal.create("reply_text_modal", "Message response").addActionRow(message).build()).queue();
    }

    @DiscordMapping(Id = "reply_text_modal")
    private void modalResponseTextMessageReply(ModalInteractionEvent event){
        String number = event.getMessage().getContentRaw().split("\n")[0].replace("Text message received from number: ", "");
        String message = event.getValue("message").getAsString();
        twilioService.sendMessage(number, message);
        event.reply("Message has been sent").queue();
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

    private void welcomeBot(Guild guild) {
        guild.createTextChannel("shlongbot")
                .addRolePermissionOverride(guild.getPublicRole().getIdLong(),
                        new LinkedList<>(),
                        new LinkedList<>(Collections.singletonList(Permission.VIEW_CHANNEL)
                        ))
                .setTopic("This is a channel made by shlongbot")
                .queue(textChannel -> textChannel.sendMessageEmbeds(EmbedMessageGenerator.welcomeMessage(guild.getOwner().getEffectiveName(), guild.getName()))
                        .queue(message -> {
                            GuildData newGuild = new GuildData();
                            newGuild.setId(guild.getIdLong());
                            newGuild.setConfigChannelId(textChannel.getIdLong());
                            newGuild.setChatroomEnabled(false);
                            newGuild.setPlanEnabled(false);
                            newGuild.setConfigMessageId(message.getIdLong());
                            guildData.save(newGuild);
                        }));
    }
}
