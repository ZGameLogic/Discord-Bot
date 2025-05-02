package com.zgamelogic.bot.services;

import com.zgamelogic.annotations.DiscordController;
import com.zgamelogic.annotations.DiscordMapping;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
@DiscordController
public class CobbleResourceService {
    private final ResourcePatternResolver resourcePatternResolver;
    private final ResourceLoader resourceLoader;
    private final long discordGuildId;
    private final List<String> maleFirstNames;
    private final List<String> femaleFirstNames;
    private final List<String> lastNames;
    @Getter
    private final Resource cobbleLogo;

    private HashMap<String, RichCustomEmoji> emojis;
    private HashMap<String, Command.Subcommand> commands;
    private Guild discordGuild;

    public CobbleResourceService(@Value("${discord.guild}") long discordGuildId, ResourcePatternResolver resourcePatternResolver, ResourceLoader resourceLoader) throws IOException {
        this.discordGuildId = discordGuildId;
        this.resourcePatternResolver = resourcePatternResolver;
        this.resourceLoader = resourceLoader;
        this.maleFirstNames = new LinkedList<>();
        this.femaleFirstNames = new LinkedList<>();
        this.lastNames = new LinkedList<>();
        cobbleLogo = resourceLoader.getResource("classpath:assets/Cobble/cobble-logo.png");
        loadNames();
    }

    /**
     * Get a mentionable string of an emoji
     * @param key
     * @return String mentionable
     */
    public String em(String key){
        try {
            return emojis.get(key).getAsMention();
        } catch(Exception e){
            return "`Emoji not found`";
        }
    }

    /**
     * Get a mentionable string of a slash command
     * @param key
     * @return String mentionable
     */
    public String cm(String key){
        try {
            return commands.get(key).getAsMention();
        } catch(Exception e){
            return "`Slash command not found`";
        }
    }

    public String randomName(boolean male) {
        Random random = new Random();
        String firstName = male ?
            maleFirstNames.get(random.nextInt(maleFirstNames.size())) :
            femaleFirstNames.get(random.nextInt(femaleFirstNames.size()));
        String lastName = lastNames.get(random.nextInt(lastNames.size()));
        return firstName + " " + lastName;
    }

    public void mapAppearance(String appearance) {
        // TODO complete
    }

    @DiscordMapping
    private void onReady(ReadyEvent event) throws IOException {
        discordGuild = event.getJDA().getGuildById(discordGuildId);
        mapEmojis();
        mapCommands();
    }

    private void loadNames() throws IOException {
        Scanner in = new Scanner(resourceLoader.getResource("classpath:assets/Cobble/female_first_names.txt").getInputStream());
        while (in.hasNextLine()) femaleFirstNames.add(in.nextLine());
        in.close();
        in = new Scanner(resourceLoader.getResource("classpath:assets/Cobble/male_first_names.txt").getInputStream());
        while (in.hasNextLine()) maleFirstNames.add(in.nextLine());
        in.close();
        in = new Scanner(resourceLoader.getResource("classpath:assets/Cobble/last_names.txt").getInputStream());
        while (in.hasNextLine()) lastNames.add(in.nextLine());
        in.close();
    }

    private void mapCommands(){
        commands = new HashMap<>();
        discordGuild.getJDA().retrieveCommands().complete().stream().filter(command -> command.getName().equals("cobble"))
            .forEach(command -> {
                command.getSubcommandGroups().forEach(subcommandGroup -> {
                    subcommandGroup.getSubcommands().forEach(subcommand ->
                        commands.put(command.getName() + " " + subcommandGroup.getName() + " " + subcommand.getName(), subcommand)
                    );
                });
                command.getSubcommands().forEach(subcommand ->
                    commands.put(command.getName() + " " + subcommand.getName(), subcommand)
                );
            });
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
