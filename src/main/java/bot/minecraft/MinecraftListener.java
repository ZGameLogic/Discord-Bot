package bot.minecraft;

import controllers.discord.EmbedMessageGenerator;
import controllers.minecraft.MinecraftAPI;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinecraftListener extends ListenerAdapter {
    private Logger logger = LoggerFactory.getLogger(MinecraftListener.class);

    @Override
    public void onReady(ReadyEvent event) {
        logger.info("Minecraft listener ready to go!");
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        try{
            if(event.getName().equals("minecraft")){
                switch(event.getSubcommandName()){
                    case "lookup":
                        handleMinecraftLookup(event);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            event.getHook().sendMessage("Unable to complete the command. If this continues to happen submit a bug report with the /bug-report command").setEphemeral(true).queue();
        }
    }

    private void handleMinecraftLookup(SlashCommandInteractionEvent event){
        event.deferReply().queue();
        String serverIP = event.getOption("server").getAsString();
        event.getHook().sendMessageEmbeds(EmbedMessageGenerator.generate(MinecraftAPI.serverStatus(serverIP))).queue();
    }
}
