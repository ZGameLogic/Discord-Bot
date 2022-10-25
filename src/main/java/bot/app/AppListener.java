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
                        Button.primary("approve_app_login", "Approve"),
                        Button.danger("deny_app_login", "Deny")
                ).queue(message -> {
                    loginRequests.saveSerialized(new LoginRequest(message.getId(), uid));
                });
            });
            return true;
        } catch (Exception e){
            logger.error(e.toString());
        }
        return false;
    }

    @GenericButtonCommand(CommandName = "approve_app_login")
    private void buttonApprove(ButtonInteractionEvent event){

    }

    @GenericButtonCommand(CommandName = "deny_app_login")
    private void buttonDeny(ButtonInteractionEvent event){

    }
}
