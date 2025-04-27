package com.zgamelogic.bot.listeners;

import com.zgamelogic.annotations.DiscordController;
import com.zgamelogic.annotations.DiscordMapping;
import com.zgamelogic.bot.services.CobbleHelperService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.FileUpload;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.util.List;

@Slf4j
@DiscordController
@AllArgsConstructor
public class CobbleBot {
    private final CobbleHelperService helperService;

    @DiscordMapping(Id = "cobble", SubId = "help")
    private void cobbleHelp(SlashCommandInteractionEvent event) throws IOException {
        event
            .replyFiles(FileUpload.fromData(getClass().getClassLoader().getResource("assets/Cobble/cobble-logo.png").openStream(), "cobble-logo.png"))
            .addEmbeds(helperService.getHelpMessage(1))
                .addActionRow(Button.secondary("cobble-help-page-prev", "Previous page").asDisabled(), Button.secondary("cobble-help-page-next", "Next Page"))
            .queue();
    }

    @DiscordMapping(Id = "cobble-help-page-next")
    @DiscordMapping(Id = "cobble-help-page-prev")
    private void cobbleHelpPageUp(ButtonInteractionEvent event) {
        long slashUserId = event.getMessage().getInteractionMetadata().getUser().getIdLong();
        if(event.getUser().getIdLong() != slashUserId){
            event.reply(helperService.PAGEABLE_PERMISSION).setEphemeral(true).queue();
            return;
        }
        int it = event.getButton().getId().equals("cobble-help-page-next") ? 1 : -1;
        int newPage = Integer.parseInt(event.getMessage().getEmbeds().get(0).getFooter().getText().replace("Page ", "")) + it;
        event.editMessageEmbeds(helperService.getHelpMessage(newPage))
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
}
