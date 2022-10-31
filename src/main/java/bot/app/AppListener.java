package bot.app;

import bot.annotations.GenericButtonCommand;
import bot.minecraft.MinecraftListener;
import bot.role.annotations.RoleButtonCommand;
import data.serializing.DataRepository;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.LinkedList;

public class AppListener extends ListenerAdapter {
    private Logger logger = LoggerFactory.getLogger(AppListener.class);
    private JDA bot;

    private DataRepository<LoginRequest> loginRequests;

    public AppListener(){
        loginRequests = new DataRepository<>("app\\LoginRequests");
    }

    @Override
    public void onReady(ReadyEvent event) {
        bot = event.getJDA();
        logger.info("App listener ready to go!");
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        try {
            String name = event.getButton().getId();
            for (Method m : getClass().getDeclaredMethods()) {
                if (m.isAnnotationPresent(GenericButtonCommand.class)) {
                    GenericButtonCommand bc = m.getAnnotation(GenericButtonCommand.class);
                    if (bc.CommandName().equals(name)) {
                        m.invoke(this, event);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean login(String uid){
        User user = bot.getUserById(uid);
        try {
            user.openPrivateChannel().queue(channel -> {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle("Login request from App");
                eb.setDescription("A login was requested from the [app name] app. Please approve or deny this request");
                eb.setColor(Color.blue);
                channel.sendMessageEmbeds(eb.build()).setActionRow(
                        Button.success("approve_app_login", "Approve"),
                        Button.danger("deny_app_login", "Deny")
                ).queue(message -> {
                    loginRequests.saveSerialized(new LoginRequest(message.getId(), uid));
                });
            });
            return true;
        } catch (Exception e){
            logger.error(e.getMessage());
        }
        return false;
    }

    public boolean isApproved(String uid){
        if(loginRequests.exists(uid)){
            return loginRequests.loadSerialized(uid).isApproved();
        }
        return false;
    }

    @GenericButtonCommand(CommandName = "approve_app_login")
    private void buttonApprove(ButtonInteractionEvent event){
        String messageID = event.getMessage().getId();
        if(loginRequests.exists(messageID)){
            LoginRequest lr = loginRequests.loadSerialized(messageID);
            lr.setApproved(true);
            loginRequests.saveSerialized(lr);
        }
        event.getMessage().editMessageComponents().queue();
        EmbedBuilder em = new EmbedBuilder(event.getMessage().getEmbeds().get(0));
        em.setColor(Color.green);
        event.editMessageEmbeds(em.build()).queue();
    }

    @GenericButtonCommand(CommandName = "deny_app_login")
    private void buttonDeny(ButtonInteractionEvent event){
        String messageID = event.getMessage().getId();
        if(loginRequests.exists(messageID)){
            LoginRequest lr = loginRequests.loadSerialized(messageID);
        }
        event.getMessage().editMessageComponents().queue();
        EmbedBuilder em = new EmbedBuilder(event.getMessage().getEmbeds().get(0));
        em.setColor(Color.red);
        event.editMessageEmbeds(em.build()).queue();
    }

    public void clearOld() {
        LinkedList<LoginRequest> oldRequests = new LinkedList<>();
        Date now = new Date();
        for(LoginRequest request : loginRequests){
            if(request.getExpires().before(now)){
                oldRequests.add(request);
            }
        }
        loginRequests.delete(oldRequests);
    }
}
