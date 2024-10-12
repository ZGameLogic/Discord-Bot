package com.zgamelogic.bot.listeners;

import com.zgamelogic.annotations.DiscordController;
import com.zgamelogic.annotations.DiscordMapping;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.session.ReadyEvent;

import java.io.File;

@Slf4j
@DiscordController
public class ArchipelagoBot {

    @DiscordMapping
    private void onReady(ReadyEvent event) {
        event.getJDA().getTextChannelById(1150451554797760593L).getIterableHistory().forEach(message -> {
            message.getAttachments().stream()
                    .filter(attachment -> attachment.getFileExtension().equals("yaml"))
                    .forEach(attachment -> {
                File file = new File("files\\" + attachment.getFileName());
                attachment.getProxy().downloadToPath(file.toPath());
            });
        });
    }
}
