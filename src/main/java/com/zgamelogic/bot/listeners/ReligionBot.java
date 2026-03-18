package com.zgamelogic.bot.listeners;

import com.zgamelogic.discord.annotations.DiscordController;
import com.zgamelogic.data.intermediates.dataotter.SlashCommandRock;
import com.zgamelogic.dataotter.DataOtterService;
import com.zgamelogic.discord.annotations.mappings.SlashCommandMapping;
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

    @SlashCommandMapping(id = "pray")
    public void praySlashCommand(SlashCommandInteractionEvent event){
        dataOtterService.sendRock(new SlashCommandRock(event));
        event.reply("Thank you, my child.").queue();
    }

    @SlashCommandMapping(id = "worship")
    public void worshipSlashCommand(SlashCommandInteractionEvent event){
        dataOtterService.sendRock(new SlashCommandRock(event));
        event.reply("Blessings upon you, for your faith is strong.").queue();
    }

    @SlashCommandMapping(id = "cry")
    public void crySlashCommand(SlashCommandInteractionEvent event){
        dataOtterService.sendRock(new SlashCommandRock(event));
        event.reply("Do not be sad, for you are a child of shlongbot. Your tears are seen, and your heart is understood. You are not alone.").queue();
    }

    @SlashCommandMapping(id = "cheer")
    public void cheerSlashCommand(SlashCommandInteractionEvent event){
        dataOtterService.sendRock(new SlashCommandRock(event));
        event.reply("Raise your spirits high! For in the realm of shlongbot, joy is never far.").queue();
    }

    @SlashCommandMapping(id = "desecrate")
    public void desecrateSlashCommand(SlashCommandInteractionEvent event){
        dataOtterService.sendRock(new SlashCommandRock(event));
        event.reply("Thou hast chosen to walk the path of chaos, yet fear not, for shlongbot oversees all in mischief and mirth alike!").queue();
    }

    @Bean
    public List<CommandData> religionSlashCommands(){
        return List.of(
                Commands.slash("pray", "Pray to our lord and savior: Shlongbot"),
                Commands.slash("worship", "Worship our lord and savior: Shlongbot"),
                Commands.slash("cry", "Cry to our lord and savior: Shlongbot"),
                Commands.slash("cheer", "Cheer to our lord and savior: Shlongbot"),
                Commands.slash("desecrate", "Desecrate our lord and savior: Shlongbot")
        );
    }
}
