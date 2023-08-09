package bot.utils;

import data.database.huntData.gun.AmmoType;
import data.database.huntData.gun.HuntGun;
import data.database.huntData.gun.HuntGunRepository;
import data.database.huntData.item.HuntItem;
import data.database.huntData.item.HuntItemRepository;
import data.intermediates.hunt.HuntLoadout;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;

public abstract class HuntHelper {
    private final static Color HUNT_COLOR = new Color(0, 0, 0);

    public static MessageEmbed initialMessage(User user){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(HUNT_COLOR);
        eb.setTitle("Hunt Randomizer");
        eb.setDescription("Using the buttons below, configure your randomizer. " +
                "Green means the setting is turned on, and red means it is off");
        eb.setFooter(user.getEffectiveName() + "'s randomizer", user.getAvatarUrl());
        return eb.build();
    }

    public static MessageEmbed loadoutMessage(HuntLoadout loadout, String imageURL, MessageEmbed original){
        EmbedBuilder eb = new EmbedBuilder(original);
        // TODO add image URL
        eb.setImage(imageURL);
        eb.clearFields();
        eb.addField("Loadout", loadout.toString(), false);
        return eb.build();
    }

    public static HuntLoadout generateLoadout(boolean dualWield, boolean quartermaster, boolean specialAmmo, boolean medkitMelee, HuntItemRepository huntItemRepository, HuntGunRepository huntGunRepository){
        // guns
        int gunBudget = quartermaster ? 5 : 4;
        // secondary fist
        LinkedList<HuntGun> gunPool = quartermaster ? huntGunRepository.findAllMediumGuns() : huntGunRepository.findAllMediumAndSmallGuns();
        if(dualWield){ // add duals if the user wants them in
            LinkedList<HuntGun> duals = huntGunRepository.findAllDuals();
            duals.forEach(gun -> gun.setSlot(HuntGun.Slot.MEDIUM));
            gunPool.addAll(duals);
        }
        Collections.shuffle(gunPool);
        HuntGun secondary = gunPool.getFirst();

        gunBudget -= secondary.getSlot() == HuntGun.Slot.MEDIUM ? 2 : 1;
        // primary second
        LinkedList<HuntGun> primaryPool = gunBudget >= 3 ? huntGunRepository.findAllLargeGuns() : huntGunRepository.findAllMediumAndSmallGuns();
        if(dualWield && gunBudget != 3){// add duals if the user wants them in
            LinkedList<HuntGun> duals = huntGunRepository.findAllDuals();
            duals.forEach(gun -> gun.setSlot(HuntGun.Slot.MEDIUM));
            primaryPool.addAll(duals);
        }
        Collections.shuffle(primaryPool);
        HuntGun primary = primaryPool.getFirst();

        LinkedList<AmmoType> primaryAmmo = new LinkedList<>();
        LinkedList<AmmoType> secondaryAmmo = new LinkedList<>();

        if(specialAmmo){
            if(primary.getSpecialAmmoCount() != 0) {
                for (int i = 0; i < primary.getSpecialAmmoCount(); i++) {
                    LinkedList<AmmoType> ammoPool =
                            !primary.hasSecondaryAmmo() ? primary.getAmmoTypes() :
                                    (i == 0 ? primary.primaryAmmo() : primary.secondaryAmmo());
                    Collections.shuffle(ammoPool);
                    if(!ammoPool.isEmpty()) primaryAmmo.add(ammoPool.get(0));
                }
            }
            if(secondary.getSpecialAmmoCount() != 0) {
                for (int i = 0; i < secondary.getSpecialAmmoCount(); i++) {
                    LinkedList<AmmoType> ammoPool =
                            !secondary.hasSecondaryAmmo() ? secondary.getAmmoTypes() :
                                    (i == 0 ? secondary.primaryAmmo() : secondary.secondaryAmmo());
                    Collections.shuffle(ammoPool);
                    if(!ammoPool.isEmpty()) secondaryAmmo.add(ammoPool.get(0));
                }
            }
        } else {
            if(primary.getSpecialAmmoCount() != 0) {
                for (int i = 0; i < primary.getSpecialAmmoCount(); i++) {
                    primaryAmmo.add(!primary.hasSecondaryAmmo() ? primary.getDefaultAmmo() : (i == 0 ? primary.getDefaultAmmo(false) : primary.getDefaultAmmo(true)));
                }
            }
            if(secondary.getSpecialAmmoCount() != 0) {
                for (int i = 0; i < secondary.getSpecialAmmoCount(); i++) {
                    secondaryAmmo.add(secondary.getDefaultAmmo());
                }
            }
        }

        // tools
        LinkedList<HuntItem> tools = new LinkedList<>();
        if(medkitMelee){
            tools.add(huntItemRepository.findById("First Aid Kit").get());
            LinkedList<HuntItem> melee = huntItemRepository.findItemsByType(HuntItem.Type.MELEE.name());
            Collections.shuffle(melee);
            tools.add(melee.getFirst());
        }
        LinkedList<HuntItem> allTools = huntItemRepository.findAllTools();
        while(tools.size() < 4){
            Collections.shuffle(allTools);
            HuntItem item = allTools.getFirst();
            if(!tools.contains(item)) tools.add(item);
        }
        // consumables
        LinkedList<HuntItem> consumables = new LinkedList<>();
        if(medkitMelee){
            LinkedList<HuntItem> healing = huntItemRepository.findItemsByType(HuntItem.Type.HEALING.name());
            Collections.shuffle(healing);
            consumables.add(healing.getFirst());
        }
        LinkedList<HuntItem> allConsumables = huntItemRepository.findAllConsumables();
        while(consumables.size() < 4){
            Collections.shuffle(allConsumables);
            consumables.add(allConsumables.getFirst());
        }

        return new HuntLoadout(primary, secondary, primaryAmmo, secondaryAmmo, tools, consumables);
    }

    public static BufferedImage generatePhoto(HuntLoadout loadout){
        final int width = 500;
        final int height = 600;
        // Constructs a BufferedImage of one of the predefined image types.
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Create a graphics which can be used to draw into the buffered image
        Graphics2D pane = bufferedImage.createGraphics();

        // background
        int num = new Random().nextInt(11) + 1;
        BufferedImage background = getAsset("background" + num + ".png");
        pane.drawImage(background, 0, 0, null);

        BufferedImage primary = getAsset(loadout.getPrimary().getAsset());
        pane.drawImage(primary, 10, 10, null);
        int xOffset = primary.getWidth() + 20;
        for(AmmoType ammo: loadout.getPrimaryAmmo()){
            BufferedImage ammoImage = getAsset(ammo.getAsset());
            pane.drawImage(ammoImage, xOffset, 10, null);
            xOffset += 60;
        }

        BufferedImage secondary = getAsset(loadout.getSecondary().getAsset());
        pane.drawImage(secondary, 10, 120, null);
        xOffset = secondary.getWidth() + 20;
        if(loadout.getSecondary().isDualWieldable() && loadout.getSecondary().getSlot() == HuntGun.Slot.MEDIUM){
            pane.drawImage(secondary, xOffset, 120, null);
            xOffset += secondary.getWidth() + 10;
        }
        for(AmmoType ammo: loadout.getSecondaryAmmo()){
            BufferedImage ammoImage = getAsset(ammo.getAsset());
            pane.drawImage(ammoImage, xOffset, 120, null);
            xOffset += 60;
        }

        xOffset = 10;
        for(HuntItem item: loadout.getTools()){
            BufferedImage ammoImage = getAsset(item.getAsset());
            pane.drawImage(ammoImage, xOffset, 230, null);
            xOffset += 95;
        }

        xOffset = 10;
        for(HuntItem item: loadout.getConsumables()){
            BufferedImage ammoImage = getAsset(item.getAsset());
            pane.drawImage(ammoImage, xOffset, 340, null);
            xOffset += 95;
        }


        return bufferedImage;
    }

    private static BufferedImage getAsset(String asset){
        try {
            return ImageIO.read(HuntHelper.class.getClassLoader().getResourceAsStream("assets/HuntShowdown/" + asset));
        } catch (Exception e){
            try {
                return ImageIO.read(HuntHelper.class.getClassLoader().getResourceAsStream("assets/HuntShowdown/unknown.png"));
            } catch (IOException ex) {
                return null;
            }
        }
    }

}
