package com.zgamelogic.bot.listeners;

import com.zgamelogic.data.database.huntData.headshot.HeadShot;
import com.zgamelogic.data.database.huntData.headshot.HeadShotRepository;
import com.zgamelogic.discord.annotations.DiscordController;
import com.zgamelogic.discord.annotations.mappings.SlashCommandMapping;
import com.zgamelogic.discord.services.ironwood.Model;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.springframework.context.annotation.Bean;

import java.time.ZonedDateTime;
import java.util.List;

@AllArgsConstructor
@DiscordController
public class HuntShowdownBot {
    private final HeadShotRepository headShotRepository;

    @SlashCommandMapping(id = "hunt", group = "headshot-counter", sub = "record", document = "headshot-record")
    public void recordHeadshot(SlashCommandInteractionEvent event, Model model){
        ZonedDateTime time = event.getTimeCreated().toZonedDateTime();
        User recorder = event.getUser();
        HeadShot shot = headShotRepository.save(new HeadShot(time, recorder.getIdLong()));
        String markdownDate = TimeFormat.DATE_SHORT_TIME_LONG.format(shot.getDate());
        model.addContext("date", markdownDate);
        model.addContext("recorder", recorder.getAsMention());
        model.addContext("count", headShotRepository.count());
    }

    @SlashCommandMapping(id = "hunt", group = "headshot-counter", sub = "statistics", document = "headshot-statistics")
    public void stats(){}

    @Bean
    public List<CommandData> huntCommands(){
        return List.of(
            Commands.slash("hunt", "All hunt related commands").addSubcommandGroups(
                new SubcommandGroupData("headshot-counter", "All headshot related commands").addSubcommands(
                    new SubcommandData("record", "Adds a record of Amrit getting headshot on a roof"),
                    new SubcommandData("statistics", "Display some statistics of the headshots")
                )
            )
        );
    }
}
