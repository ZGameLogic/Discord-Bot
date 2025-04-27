package com.zgamelogic.bot.listeners;

import com.zgamelogic.annotations.DiscordController;
import com.zgamelogic.annotations.DiscordMapping;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.FileUpload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.zgamelogic.bot.utils.CobbleHelper.*;

@Slf4j
@DiscordController
public class CobbleBot {
    private final long discordGuildId;
    private final ResourcePatternResolver resourcePatternResolver;

    private HashMap<Long, RichCustomEmoji> emojis;
    private Guild discordGuild;

    public CobbleBot(@Value("${discord.guild}") long discordGuildId, ResourcePatternResolver resourcePatternResolver) {
        this.discordGuildId = discordGuildId;
        this.resourcePatternResolver = resourcePatternResolver;
    }

    @DiscordMapping
    private void onReady(ReadyEvent event) throws IOException, URISyntaxException {
        discordGuild = event.getJDA().getGuildById(discordGuildId);
        mapEmojis();
    }

    @DiscordMapping(Id = "cobble", SubId = "help")
    private void cobbleHelp(SlashCommandInteractionEvent event) throws IOException {
        event
            .replyFiles(FileUpload.fromData(getClass().getClassLoader().getResource("assets/Cobble/cobble-logo.png").openStream(), "cobble-logo.png"))
            .addEmbeds(getHelpMessage(1))
                .addActionRow(Button.secondary("cobble-help-page-prev", "Previous page").asDisabled(), Button.secondary("cobble-help-page-next", "Next Page"))
            .queue();
    }

    @DiscordMapping(Id = "cobble-help-page-next")
    @DiscordMapping(Id = "cobble-help-page-prev")
    private void cobbleHelpPageUp(ButtonInteractionEvent event) {
        long slashUserId = event.getMessage().getInteractionMetadata().getUser().getIdLong();
        if(event.getUser().getIdLong() != slashUserId){
            event.reply(PAGEABLE_PERMISSION).setEphemeral(true).queue();
            return;
        }
        int it = event.getButton().getId().equals("cobble-help-page-next") ? 1 : -1;
        int newPage = Integer.parseInt(event.getMessage().getEmbeds().get(0).getFooter().getText().replace("Page ", "")) + it;
        event.editMessageEmbeds(getHelpMessage(newPage))
            .setActionRow(
                Button.secondary("cobble-help-page-prev", "Previous page").withDisabled(newPage == 1),
                Button.secondary("cobble-help-page-next", "Next Page").withDisabled(newPage == 3)
            ).queue();
    }

    @Bean
    public List<CommandData> cobbleCommands(){
        return List.of(
            Commands.slash("cobble", "All commands related to the cobble game.").addSubcommands(
                new SubcommandData("help", "Get some idea on how to play the game.")
            )
        );
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
                emojis.put(emoji.getIdLong(), emoji);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
