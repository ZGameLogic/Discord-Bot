package com.zgamelogic.bot.listeners;

import com.zgamelogic.annotations.DiscordController;
import com.zgamelogic.annotations.DiscordMapping;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

@Slf4j
@DiscordController
public class CobbleBot {
    @Value("${discord.guild}")
    private long discordGuildId;

    private HashMap<Long, RichCustomEmoji> emojis;

    @DiscordMapping
    private void onReady(ReadyEvent event) throws IOException, URISyntaxException {
        Guild discordGuild = event.getJDA().getGuildById(discordGuildId);
        emojis = new HashMap<>();
        Files.walk(Paths.get(getClass().getClassLoader().getResource("assets/Cobble").toURI()))
            .map(Path::toFile)
            .filter(file -> file.getName().endsWith(".png"))
            .forEach(iconFile -> {
                int dotIndex = iconFile.getName().lastIndexOf('.');
                String filename = iconFile.getName().substring(0, dotIndex);
                List<RichCustomEmoji> existing = discordGuild.getEmojisByName(filename, true);
                if(existing.isEmpty()) {
                    try {
                        Icon icon = Icon.from(iconFile);
                        RichCustomEmoji emoji = discordGuild.createEmoji(filename, icon).complete();
                        emojis.put(emoji.getIdLong(), emoji);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    emojis.put(existing.get(0).getIdLong(), existing.get(0));
                }
            });
    }

    @Bean
    public List<CommandData> cobbleCommands(){
        return List.of();
    }
}
