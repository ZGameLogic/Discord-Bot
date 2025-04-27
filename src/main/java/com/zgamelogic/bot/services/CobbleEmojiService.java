package com.zgamelogic.bot.services;

import com.zgamelogic.annotations.DiscordController;
import com.zgamelogic.annotations.DiscordMapping;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Service
@DiscordController
public class CobbleEmojiService {
    private final ResourcePatternResolver resourcePatternResolver;
    private final long discordGuildId;

    private HashMap<String, RichCustomEmoji> emojis;
    private Guild discordGuild;

    public CobbleEmojiService(@Value("${discord.guild}") long discordGuildId, ResourcePatternResolver resourcePatternResolver) {
        this.discordGuildId = discordGuildId;
        this.resourcePatternResolver = resourcePatternResolver;
    }

    public String km(String key){
        return emojis.get(key).getAsMention();
    }

    @DiscordMapping
    private void onReady(ReadyEvent event) throws IOException {
        discordGuild = event.getJDA().getGuildById(discordGuildId);
        mapEmojis();
    }

    private void mapEmojis() throws IOException {
        emojis = new HashMap<>();
        Arrays.stream(resourcePatternResolver.getResources("classpath:assets/Cobble/Emojis/*")).forEach(resource -> {
            try {
                String filename = resource.getFilename();
                String iconName = filename.replace(".png", "");
                List<RichCustomEmoji> emojiList = discordGuild.getEmojisByName(iconName, true);
                RichCustomEmoji emoji;
                if(emojiList.isEmpty()) {
                    Icon icon = Icon.from(resource.getInputStream());
                    emoji = discordGuild.createEmoji(iconName, icon).complete();
                } else {
                    emoji = emojiList.get(0);
                }
                emojis.put(emoji.getName(), emoji);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
