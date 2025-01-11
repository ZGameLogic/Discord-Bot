package com.zgamelogic.bot.listeners;

import com.zgamelogic.annotations.DiscordController;
import com.zgamelogic.annotations.DiscordMapping;
import com.zgamelogic.data.intermediates.dataotter.SlashCommandRock;
import com.zgamelogic.dataotter.DataOtterService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.context.annotation.Bean;

import java.util.List;

@DiscordController
public class ReligionBot {
    private final DataOtterService dataOtterService;

    public ReligionBot(DataOtterService dataOtterService) {
        this.dataOtterService = dataOtterService;
    }

    @DiscordMapping(Id = "pray")
    private void praySlashCommand(SlashCommandInteractionEvent event){
        dataOtterService.sendRock(new SlashCommandRock(event));
        event.reply("Thank you, my child.").queue();
    }

    @DiscordMapping(Id = "worship")
    private void worshipSlashCommand(SlashCommandInteractionEvent event){
        dataOtterService.sendRock(new SlashCommandRock(event));
        event.reply("Blessings upon you, for your faith is strong.").queue();
    }

    @DiscordMapping(Id = "cry")
    private void crySlashCommand(SlashCommandInteractionEvent event){
        dataOtterService.sendRock(new SlashCommandRock(event));
        event.reply("Do not be sad, for you are a child of shlongbot. Your tears are seen, and your heart is understood. You are not alone.").queue();
    }

    @DiscordMapping(Id = "cheer")
    private void cheerSlashCommand(SlashCommandInteractionEvent event){
        dataOtterService.sendRock(new SlashCommandRock(event));
        event.reply("Raise your spirits high! For in the realm of shlongbot, joy is never far.").queue();
    }

    @DiscordMapping(Id = "desecrate")
    private void desecrateSlashCommand(SlashCommandInteractionEvent event){
        dataOtterService.sendRock(new SlashCommandRock(event));
        event.reply("Thou hast chosen to walk the path of chaos, yet fear not, for shlongbot oversees all in mischief and mirth alike!").queue();
    }

    @Bean
    private List<CommandData> religionSlashCommands(){
        return List.of(
                Commands.slash("pray", "Pray to our lord and savior: Shlongbot"),
                Commands.slash("worship", "Worship our lord and savior: Shlongbot"),
                Commands.slash("cry", "Cry to our lord and savior: Shlongbot"),
                Commands.slash("cheer", "Cheer to our lord and savior: Shlongbot"),
                Commands.slash("desecrate", "Desecrate our lord and savior: Shlongbot")
        );
    }
}
