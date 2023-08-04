package bot.listeners;

import application.App;
import com.zgamelogic.AdvancedListenerAdapter;
import data.database.guildData.GuildData;
import data.database.guildData.GuildDataRepository;
import data.database.huntData.gun.AmmoType;
import data.database.huntData.gun.HuntGun;
import data.database.huntData.gun.HuntGunRepository;
import data.database.huntData.item.HuntItem;
import data.database.huntData.item.HuntItemRepository;
import data.intermediates.hunt.HuntLoadout;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.FileUpload;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;

import static bot.utils.HuntHelper.*;

public class HuntShowdownBot extends AdvancedListenerAdapter {

    private final GuildDataRepository guildData;
    private final HuntGunRepository huntGunRepository;
    private final HuntItemRepository huntItemRepository;

    public HuntShowdownBot(GuildDataRepository guildData, HuntGunRepository huntGunRepository, HuntItemRepository huntItemRepository) {
        this.guildData = guildData;
        this.huntGunRepository = huntGunRepository;
        this.huntItemRepository = huntItemRepository;
    }

    @ButtonResponse("enable_hunt")
    private void enableHunt(ButtonInteractionEvent event){
        event.editButton(Button.success("disable_hunt", "Hunt bot")).queue();
        Guild guild = event.getGuild();
        long randomizerCommandId = guild.upsertCommand(
                Commands.slash("hunt", "Hunt Showdown commands")
                        .addSubcommands(
                                new SubcommandData("randomizer", "Summons the randomizer")
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
        event.replyEmbeds(initialMessage(event.getUser()))
                .addActionRow(
                        Button.primary("randomize", "Randomize"),
                        Button.success("disable_dual", "Dual Wielding"),
                        Button.danger("enable_quarter", "Quartermaster"),
                        Button.success("disable_special", "Special Ammo"),
                        Button.success("disable_medkit_melee", "Healing & Melee")
                ).queue();
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

//        for(HuntGun gun: huntGunRepository.findAll()){
//            try {
//                if(gun.getAsset() != null){
//                    ImageIO.read(HuntShowdownBot.class.getClassLoader().getResourceAsStream("assets/HuntShowdown/" + gun.getAsset()));
//                } else {
//                    System.out.println("Asset is null for " + gun.getName());
//                }
//            } catch(Exception e) {
//                System.out.println("Unable to load asset for " + gun.getName() + "\n\t" + gun.getAsset());
//            }
//            for(AmmoType ammo: gun.getAmmoTypes()){
//                try {
//                    if(ammo.getAsset() != null) {
//                        ImageIO.read(HuntShowdownBot.class.getClassLoader().getResourceAsStream("assets/HuntShowdown/" + ammo.getAsset()));
//                    } else {
//                        System.out.println("Asset is null for " + ammo.getName());
//                    }
//                } catch(Exception e) {
//                    System.out.println("Unable to load asset for " + ammo.getName() + "\n\t" + ammo.getAsset());
//                }
//            }
//        }

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

    //    private void spawnDungeon() {
//        GameConfigValues config = data.getGameConfig().loadSerialized();
//        TextChannel dungeonsTC = guild.getTextChannelById(config.getChannelIds().get("dungeons"));
//        Emoji activate = guild.getEmojiById(config.getIconIds().get("Activate"));
//        Dungeon dungeon = DungeonGenerator.GenerateRandomDungeon();
//        DungeonGenerator.saveDungeon(dungeon);
//        File dungeonPhoto = new File("arena\\dungeon photos\\dungeon.png");
//        logger.info("\tSpawned dungeon");
//        Message message = dungeonsTC.sendFiles(FileUpload.fromData(dungeonPhoto)).setEmbeds(EmbedMessageGenerator.generate(dungeon)).complete();
//        dungeon.setId(message.getId());
//        message.addReaction(activate).queue();
//        data.saveData(dungeon);
//        dungeonPhoto.renameTo(new File("arena\\dungeon photos\\" + dungeon.getId() + ".png"));
//    }

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
