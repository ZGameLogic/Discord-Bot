package bot.listeners;

import com.zgamelogic.AdvancedListenerAdapter;
import data.database.guildData.GuildData;
import data.database.guildData.GuildDataRepository;
import data.database.huntData.gun.AmmoType;
import data.database.huntData.gun.HuntGun;
import data.database.huntData.gun.HuntGunRepository;
import data.database.huntData.item.HuntItem;
import data.database.huntData.item.HuntItemRepository;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.LinkedList;

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

        System.out.println(generateLoadout(
                dualWielding,
                quartermaster,
                specialAmmo,
                medkitMelee,
                huntItemRepository,
                huntGunRepository
        ));


        // huntItemRepository.findItemsByType(HuntItem.Type.TOOL.name()).forEach(tool -> System.out.println(tool.getName()));

//        LinkedList<AmmoType> ammos = new LinkedList<>();
//        ammos.add(new AmmoType("Poison", "poison.png", true, false));
//        ammos.add(new AmmoType("Full Metal Jacket", "fmj.png", true, false));
//        ammos.add(new AmmoType("Long Ammo", "long.png", false, false));
//        HuntGun gun = new HuntGun("Test gun 1", HuntGun.Slot.LARGE, ammos, 1, false, "placeholder.png");
//        huntGunRepository.save(gun);
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
