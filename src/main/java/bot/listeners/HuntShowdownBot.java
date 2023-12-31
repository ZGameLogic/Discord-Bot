package bot.listeners;

import application.App;
import com.zgamelogic.AdvancedListenerAdapter;
import data.database.guildData.GuildData;
import data.database.guildData.GuildDataRepository;
import data.database.huntData.gun.HuntGunRepository;
import data.database.huntData.item.HuntItemRepository;
import data.database.huntData.randomizer.HuntRandomizerRepository;
import data.database.huntData.randomizer.RandomizerData;
import data.intermediates.hunt.HuntLoadout;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.FileUpload;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static bot.utils.HuntHelper.*;

@Slf4j
public class HuntShowdownBot extends AdvancedListenerAdapter {

    private final GuildDataRepository guildData;
    private final HuntGunRepository huntGunRepository;
    private final HuntItemRepository huntItemRepository;
    private final HuntRandomizerRepository huntRandomizerRepository;

    public HuntShowdownBot(GuildDataRepository guildData, HuntGunRepository huntGunRepository, HuntItemRepository huntItemRepository, HuntRandomizerRepository huntRandomizerRepository) {
        this.guildData = guildData;
        this.huntGunRepository = huntGunRepository;
        this.huntItemRepository = huntItemRepository;
        this.huntRandomizerRepository = huntRandomizerRepository;
    }

    @ButtonResponse("enable_hunt")
    private void enableHunt(ButtonInteractionEvent event){
        event.editButton(Button.success("disable_hunt", "Hunt bot")).queue();
        Guild guild = event.getGuild();
        long randomizerCommandId = guild.upsertCommand(
                Commands.slash("hunt", "Hunt Showdown commands")
                        .addSubcommands(
                                new SubcommandData("randomizer", "Summons the randomizer"),
                                new SubcommandData("reroll", "Reroll a weapon, tool, or consumable you don't have")
                                        .addOption(OptionType.STRING, "item", "Weapon, tool, or consumable to re-roll", true, true)
                        )
        ).complete().getIdLong();
        GuildData dbGuild = guildData.getOne(guild.getIdLong());
        dbGuild.setHuntEnabled(true);
        dbGuild.setRandomizerSummonId(randomizerCommandId);
        guildData.save(dbGuild);
    }

    @ButtonResponse("disable_hunt")
    private void disableHunt(ButtonInteractionEvent event){
        event.editButton(Button.danger("enable_hunt", "Hunt bot")).queue();
        Guild guild = event.getGuild();
        GuildData dbGuild = guildData.getOne(guild.getIdLong());
        guild.deleteCommandById(dbGuild.getRandomizerSummonId()).queue();
        dbGuild.setHuntEnabled(false);
        guildData.save(dbGuild);
    }

    @SlashResponse(value = "hunt", subCommandName = "randomizer")
    private void summonRandomizer(SlashCommandInteractionEvent event){
        String uid = event.getUser().getId();
        Optional<RandomizerData> oldRando = huntRandomizerRepository.getByUserId(uid);
        oldRando.ifPresent(randomizerData -> {
            try {
                event.getJDA()
                        .getGuildById(randomizerData.getGuildId())
                        .getChannelById(GuildMessageChannel.class, randomizerData.getChannelId())
                        .deleteMessageById(randomizerData.getMessageId()).queue();
            } catch (Exception ignored){}
            huntRandomizerRepository.delete(randomizerData);
        });

        event.replyEmbeds(initialMessage(event.getUser()))
                .addActionRow(
                        Button.primary("randomize", "Randomize"),
                        Button.success("disable_dual", "Dual Wielding"),
                        Button.danger("enable_quarter", "Quartermaster"),
                        Button.success("disable_special", "Special Ammo"),
                        Button.success("disable_medkit_melee", "Healing & Melee")
                ).queue(interactionHook -> {
                    interactionHook.retrieveOriginal().queue(message -> {
                        huntRandomizerRepository.save(new RandomizerData(
                                message.getIdLong(),
                                event.getUser().getIdLong(),
                                message.getChannel().getIdLong(),
                                message.getGuild().getIdLong()
                        ));
                    });
                });
    }

    @AutoCompleteResponse(slashCommandId = "hunt", slashSubCommandId = "reroll", focusedOption = "item")
    private void rerollItemAutoCompleteResponse(CommandAutoCompleteInteractionEvent event){
        String userId = event.getUser().getId();
        huntRandomizerRepository.getByUserId(userId).ifPresent(randomizerData -> {
            Message message = event.getJDA()
                    .getGuildById(randomizerData.getGuildId())
                    .getChannelById(GuildMessageChannel.class, randomizerData.getChannelId())
                    .retrieveMessageById(randomizerData.getMessageId())
                    .complete();
            HuntLoadout loadout = null;
            try {
                loadout = getLoadoutFromEmbed(
                        message.getEmbeds().get(0),
                        huntGunRepository,
                        huntItemRepository
                );
            } catch (Exception e){
                event.replyChoices().queue();
                return;
            }
            event.replyChoices(
                    Stream.of(loadout.convertToSlashCommandOptions())
                            .filter(word -> word.toLowerCase().startsWith(event.getFocusedOption().getValue().toLowerCase()))
                            .map(word -> new Command.Choice(word, word))
                            .collect(Collectors.toList())
            ).queue();
        });

        if(!huntRandomizerRepository.getByUserId(userId).isPresent()){
            event.replyChoices().queue();
        }
    }

    @SlashResponse(value = "hunt", subCommandName = "reroll")
    private void rerollSlashCommand(SlashCommandInteractionEvent event){
        if(event.getOption("item") == null) {
            event.reply("no item was given to reroll").setEphemeral(true).queue();
        }
        huntRandomizerRepository.getByUserId(event.getUser().getId()).ifPresent(randomizerData -> {
            String item = event.getOption("item").getAsString();
            Message message = event.getJDA()
                    .getGuildById(randomizerData.getGuildId())
                    .getChannelById(GuildMessageChannel.class, randomizerData.getChannelId())
                    .retrieveMessageById(randomizerData.getMessageId())
                    .complete();
            HuntLoadout loadout = null;
            try {
                loadout = getLoadoutFromEmbed(
                        message.getEmbeds().get(0),
                        huntGunRepository,
                        huntItemRepository
                );
            } catch (Exception e){
                event.reply("Unable to read loadout. Have you rolled yet?").setEphemeral(true).queue();
                return;
            }
            if(!loadout.hasItem(item)){
                event.reply("Your loadout does not contain this item").setEphemeral(true).queue();
                return;
            }
            loadout.removeItemFromLoadout(item);

            boolean dualWielding = false;
            boolean quartermaster = false;
            boolean specialAmmo = false;
            boolean medkitMelee = false;
            for(ItemComponent button: message.getActionRows().get(0).getComponents()){
                switch(((Button) button).getId()){
                    case "disable_dual": dualWielding = true; break;
                    case "disable_quarter": quartermaster = true; break;
                    case "disable_special": specialAmmo = true; break;
                    case "disable_medkit_melee": medkitMelee = true; break;
                }
            }

            loadout = generateLoadout(
                    loadout,
                    item,
                    dualWielding,
                    quartermaster,
                    specialAmmo,
                    medkitMelee,
                    huntItemRepository,
                    huntGunRepository
            );

            // create picture
            try {
                File tempFile = new File(new Random().nextInt() + ".png");
                ImageIO.write(generatePhoto(loadout), "png", tempFile);
                // upload picture
                Message photoMessage = message.getGuild().getTextChannelById(App.config.getLoadoutChatId()).sendFiles(FileUpload.fromData(tempFile)).complete();
                String loadoutUrl = photoMessage.getAttachments().get(0).getUrl();
                message.editMessageEmbeds(
                        loadoutMessage(loadout, loadoutUrl, message.getEmbeds().get(0))
                ).queue();
                tempFile.delete();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            event.reply("rerolled " + item + " from previous loadout").setEphemeral(true).queue();
        });
        if(!huntRandomizerRepository.getByUserId(event.getUser().getId()).isPresent()){
            event.reply("Cannot find hunt randomizor message for you").setEphemeral(true).queue();
        }
    }

    @ButtonResponse("randomize")
    private void randomize(ButtonInteractionEvent event){
        if(preflight(event)) return;
        boolean dualWielding = false;
        boolean quartermaster = false;
        boolean specialAmmo = false;
        boolean medkitMelee = false;
        for(ItemComponent button: event.getMessage().getActionRows().get(0).getComponents()){
            switch(((Button) button).getId()){
                case "disable_dual": dualWielding = true; break;
                case "disable_quarter": quartermaster = true; break;
                case "disable_special": specialAmmo = true; break;
                case "disable_medkit_melee": medkitMelee = true; break;
            }
        }

        Message message = event.getMessage();
        HuntLoadout loadout = generateLoadout(
                dualWielding,
                quartermaster,
                specialAmmo,
                medkitMelee,
                huntItemRepository,
                huntGunRepository
        );

        // create picture
        try {
            File tempFile = new File(new Random().nextInt() + ".png");
            ImageIO.write(generatePhoto(loadout), "png", tempFile);
            // upload picture
            Message photoMessage = message.getGuild().getTextChannelById(App.config.getLoadoutChatId()).sendFiles(FileUpload.fromData(tempFile)).complete();
            String loadoutUrl = photoMessage.getAttachments().get(0).getUrl();
            message.editMessageEmbeds(
                    loadoutMessage(loadout, loadoutUrl, message.getEmbeds().get(0))
            ).queue();
            tempFile.delete();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        event.deferEdit().queue();
    }

    @ButtonResponse("disable_dual")
    private void disableDualWielding(ButtonInteractionEvent event){
        if(preflight(event)) return;
        event.editButton(Button.danger("enable_dual", "Dual Wielding")).queue();
    }

    @ButtonResponse("disable_quarter")
    private void disableQuartermaster(ButtonInteractionEvent event){
        if(preflight(event)) return;
        event.editButton(Button.danger("enable_quarter", "Quartermaster")).queue();
    }

    @ButtonResponse("disable_special")
    private void disableSpecialAmmo(ButtonInteractionEvent event){
        if(preflight(event)) return;
        event.editButton(Button.danger("enable_special", "Special Ammo")).queue();
    }

    @ButtonResponse("disable_medkit_melee")
    private void disableMedkitMelee(ButtonInteractionEvent event){
        if(preflight(event)) return;
        event.editButton(Button.danger("enable_medkit_melee", "Healing & Melee")).queue();
    }

    @ButtonResponse("enable_dual")
    private void enableDualWielding(ButtonInteractionEvent event){
        if(preflight(event)) return;
        event.editButton(Button.success("disable_dual", "Dual Wielding")).queue();
    }

    @ButtonResponse("enable_quarter")
    private void enableQuartermaster(ButtonInteractionEvent event){
        if(preflight(event)) return;
        event.editButton(Button.success("disable_quarter", "Quartermaster")).queue();
    }

    @ButtonResponse("enable_special")
    private void enableSpecialAmmo(ButtonInteractionEvent event){
        if(preflight(event)) return;
        event.editButton(Button.success("disable_special", "Special Ammo")).queue();
    }

    @ButtonResponse("enable_medkit_melee")
    private void enableMedkitMelee(ButtonInteractionEvent event){
        if(preflight(event)) return;
        event.editButton(Button.success("disable_medkit_melee", "Healing & Melee")).queue();
    }

    private boolean preflight(ButtonInteractionEvent event){
        if(event.getMessage().getEmbeds().get(0).getFooter().getText().contains(event.getUser().getEffectiveName())){
            return false;
        }
        event.reply("Only the creator of this randomizer can edit settings. If you wish to make your own, use the slash command: /hunt randomizer").setEphemeral(true).queue();
        return true;
    }
}
