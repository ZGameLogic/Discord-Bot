package com.zgamelogic.bot.listeners;

import com.zgamelogic.annotations.DiscordController;
import com.zgamelogic.annotations.DiscordMapping;
import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.springframework.context.annotation.Bean;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@DiscordController
public class ArchipelagoBot {
    private final static String PLAYER_BASE_PATH = "players";

    @PostConstruct
    public void init() {
        File base = new File(PLAYER_BASE_PATH);
        if(!base.exists()){
            base.mkdirs();
        }
    }

    @DiscordMapping(Id = "archipelago", SubId = "collect")
    private void collectionArchipelago(SlashCommandInteractionEvent event){
        if(event.getChannel().getType() != ChannelType.GUILD_PUBLIC_THREAD) {
            event.reply("This has to be done in a thread").setEphemeral(true).queue();
            return;
        }
        event.deferReply().queue();
        File base = new File(PLAYER_BASE_PATH);
        for(File existing: base.listFiles()) existing.delete();
        ThreadChannel channel = event.getChannel().asThreadChannel();
        List<String> files = new ArrayList<>();
        channel.getIterableHistory().stream()
                .flatMap(message -> message.getAttachments().stream())
                .filter(attachment -> attachment.getFileExtension().equals("yaml"))
                        .forEach(attachment -> {
                            File file = new File(PLAYER_BASE_PATH + "/" + attachment.getFileName());
                            files.add(attachment.getUrl());
                            attachment.getProxy().downloadToPath(file.toPath());
                        });

        event.getHook().sendMessage("Collected these files: " + String.join(", ", files)).queue();
    }

    @Bean
    public SlashCommandData archipelagoCommand() {
        return Commands.slash("archipelago", "A command for all archipelago commands")
                .addSubcommands(
                        new SubcommandData("collect", "Collect all the yamls for a run")
                );
    }
}
