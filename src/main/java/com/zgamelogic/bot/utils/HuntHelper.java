package com.zgamelogic.bot.utils;

import com.zgamelogic.data.database.huntData.gun.AmmoType;
import com.zgamelogic.data.database.huntData.gun.HuntGun;
import com.zgamelogic.data.database.huntData.gun.HuntGunRepository;
import com.zgamelogic.data.database.huntData.item.HuntItem;
import com.zgamelogic.data.database.huntData.item.HuntItemRepository;
import com.zgamelogic.data.intermediates.hunt.HuntLoadout;
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
        eb.setImage(imageURL);
        eb.clearFields();
        eb.addField("Loadout", loadout.toString(), false);
        return eb.build();
    }

    public static HuntLoadout getLoadoutFromEmbed(MessageEmbed message, HuntGunRepository gunRepository, HuntItemRepository itemRepository) {
        String loadout = message.getFields().get(0).getValue();
        String primary = loadout.split("\n")[0];
        String secondary = loadout.split("\n")[1];
        String tools = loadout.split("\n")[2];
        String consumables = loadout.split("\n")[3];

        HuntGun primaryGun;
        String gun = primary.split(":")[0];
        if(gun.contains("Dual ")) {
            primaryGun = gunRepository.getOne(primary.split(":")[0].replace("Dual ", ""));
            primaryGun.setSlot(HuntGun.Slot.MEDIUM);
        } else {
            primaryGun = gunRepository.getOne(primary.split(":")[0]);
        }

        HuntGun secondaryGun;
        gun = secondary.split(":")[0];
        if(gun.contains("Dual ")) {
            secondaryGun = gunRepository.getOne(secondary.split(":")[0].replace("Dual ", ""));
            secondaryGun.setSlot(HuntGun.Slot.MEDIUM);
        } else {
            secondaryGun = gunRepository.getOne(secondary.split(":")[0]);
        }

        LinkedList<AmmoType> primaryAmmo = new LinkedList<>();
        if(primary.split(":").length > 1){
            for(String ammoName: primary.split(":")[1].split("\\|")){
                if(ammoName != null && !ammoName.trim().isEmpty()) primaryAmmo.add(primaryGun.getAmmoTypeFromString(ammoName.trim()));
            }
        }

        LinkedList<AmmoType> secondaryAmmo = new LinkedList<>();
        if(secondary.split(":").length > 1){
            for(String ammoName: secondary.split(":")[1].split("\\|")){
                if(ammoName != null && !ammoName.trim().isEmpty()) secondaryAmmo.add(secondaryGun.getAmmoTypeFromString(ammoName.trim()));
            }
        }

        LinkedList<HuntItem> toolsList = new LinkedList<>();
        for(String tool: tools.split(",")){
            itemRepository.findById(tool.trim()).ifPresent(toolsList::add);
        }

        LinkedList<HuntItem> consumablesList = new LinkedList<>();
        for(String consumable: consumables.split(",")){
            itemRepository.findById(consumable.trim()).ifPresent(consumablesList::add);
        }

        HuntLoadout huntLoadout = new HuntLoadout();
        huntLoadout.setPrimary(primaryGun);
        huntLoadout.setPrimaryAmmo(primaryAmmo);
        huntLoadout.setSecondary(secondaryGun);
        huntLoadout.setSecondaryAmmo(secondaryAmmo);
        huntLoadout.setTools(toolsList);
        huntLoadout.setConsumables(consumablesList);

        return huntLoadout;
    }

    public static HuntLoadout generateLoadout(HuntLoadout loadout, String item, boolean dualWield, boolean quartermaster, boolean specialAmmo, boolean medkitMelee, HuntItemRepository huntItemRepository, HuntGunRepository huntGunRepository){

        // guns
        int gunBudget = quartermaster ? 5 : 4;
        if(loadout.getPrimary() != null){
            gunBudget -= loadout.getPrimary().getSlot() == HuntGun.Slot.MEDIUM ? 2 : loadout.getPrimary().getSlot() == HuntGun.Slot.SMALL ? 1 : 3;
        }
        // secondary fist
        while(loadout.getSecondary() == null) {
            LinkedList<HuntGun> gunPool = gunBudget >= 2 ?
                    huntGunRepository.findAllMediumGuns()
                    : huntGunRepository.findAllSmallGuns();
            if (dualWield) { // add duals if the user wants them in
                LinkedList<HuntGun> duals = huntGunRepository.findAllDuals();
                duals.forEach(gun -> gun.setSlot(HuntGun.Slot.MEDIUM));
                gunPool.addAll(duals);
            }
            Collections.shuffle(gunPool);
            HuntGun gun = gunPool.getFirst();
            if(!gun.getName().equals(item)){
                loadout.setSecondary(gun);
                LinkedList<AmmoType> ammos = new LinkedList<>();
                for (int i = 0; i < gun.getSpecialAmmoCount() ; i++) ammos.add(null);
                loadout.setSecondaryAmmo(ammos);
            }
        }
        gunBudget -= loadout.getSecondary().getSlot() == HuntGun.Slot.MEDIUM ? 2 : 1;
        // primary second
        while(loadout.getPrimary() == null) {
            LinkedList<HuntGun> primaryPool = gunBudget == 3 ? huntGunRepository.findAllLargeGuns() : huntGunRepository.findAllMediumGuns();
            if (dualWield && gunBudget != 3) {// add duals if the user wants them in
                LinkedList<HuntGun> duals = huntGunRepository.findAllDuals();
                duals.forEach(gun -> gun.setSlot(HuntGun.Slot.MEDIUM));
                primaryPool.addAll(duals);
            }
            Collections.shuffle(primaryPool);
            HuntGun gun = primaryPool.getFirst();
            if(!gun.getName().equals(item)){
                loadout.setPrimary(gun);
                LinkedList<AmmoType> ammos = new LinkedList<>();
                for (int i = 0; i < gun.getSpecialAmmoCount() ; i++) ammos.add(null);
                loadout.setPrimaryAmmo(ammos);
            }
        }

        item = item.replace(loadout.getPrimary().getName(), "").replace(loadout.getSecondary().getName(), "").trim();

        // primary ammo types
        while(loadout.getPrimaryAmmo().contains(null)){
            AmmoType ammo = null;
            int i = loadout.getPrimaryAmmo().indexOf(null);
            if(specialAmmo){
                    LinkedList<AmmoType> ammoPool =
                            !loadout.getPrimary().hasSecondaryAmmo() ? loadout.getPrimary().getAmmoTypes() :
                                    (i == 0 ? loadout.getPrimary().primaryAmmo() : loadout.getPrimary().secondaryAmmo());
                    Collections.shuffle(ammoPool);
                    if(!ammoPool.isEmpty()) ammo = ammoPool.get(0);
            } else {
                    ammo = !loadout.getPrimary().hasSecondaryAmmo() ? loadout.getPrimary().getDefaultAmmo() : (i == 0 ? loadout.getPrimary().getDefaultAmmo(false) : loadout.getPrimary().getDefaultAmmo(true));
            }
            if (ammo != null && (!ammo.getName().equals(item) || !specialAmmo)) {
                loadout.getPrimaryAmmo().set(loadout.getPrimaryAmmo().indexOf(null), ammo);
            }
        }

        // secondary ammo types
        while(loadout.getSecondaryAmmo().contains(null)) {
            AmmoType ammo = null;
            int i = loadout.getPrimaryAmmo().indexOf(null);
            if (specialAmmo) {
                LinkedList<AmmoType> ammoPool =
                        !loadout.getSecondary().hasSecondaryAmmo() ? loadout.getSecondary().getAmmoTypes() :
                                (i == 0 ? loadout.getSecondary().primaryAmmo() : loadout.getSecondary().secondaryAmmo());
                Collections.shuffle(ammoPool);
                if (!ammoPool.isEmpty()) ammo = ammoPool.get(0);
            } else {
                ammo = loadout.getSecondary().getDefaultAmmo();
            }
            if (ammo != null && (!ammo.getName().equals(item) || !specialAmmo)) {
                loadout.getSecondaryAmmo().set(loadout.getSecondaryAmmo().indexOf(null), ammo);
            }
        }

        // tools
        while(loadout.getTools().contains(null)){
            LinkedList<HuntItem> tools = new LinkedList<>();
            if(medkitMelee){
                if(loadout.getTools().indexOf(null) == 0){
                    tools.add(huntItemRepository.findById("First Aid Kit").get());
                } else if(loadout.getTools().indexOf(null) == 1){
                    tools.addAll(huntItemRepository.findItemsByType(HuntItem.Type.MELEE.name()));
                } else {
                    tools.addAll(huntItemRepository.findAllTools());
                }
            } else {
                tools.addAll(huntItemRepository.findAllTools());
            }
            Collections.shuffle(tools);
            HuntItem tool = tools.getFirst();
            if(!loadout.getTools().contains(tool) && (!tool.getName().equals(item) || tool.getName().equals("First Aid Kit"))) loadout.getTools().set(loadout.getTools().indexOf(null), tool);
        }
        // consumables
        while(loadout.getConsumables().contains(null)){
            LinkedList<HuntItem> consumables = new LinkedList<>();

            if(medkitMelee){
                consumables.addAll(huntItemRepository.findItemsByType(HuntItem.Type.HEALING.name()));
            } else {
                consumables.addAll(huntItemRepository.findAllConsumables());
            }

            Collections.shuffle(consumables);
            HuntItem consumable = consumables.getFirst();
            if(!consumable.getName().equals(item)) loadout.getConsumables().set(loadout.getConsumables().indexOf(null), consumable);
        }

        return loadout;
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
        LinkedList<HuntGun> primaryPool = gunBudget == 3 ? huntGunRepository.findAllLargeGuns() : huntGunRepository.findAllMediumGuns();
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
        if(loadout.getPrimary().isDualWieldable() && loadout.getPrimary().getSlot() == HuntGun.Slot.MEDIUM){
            pane.drawImage(primary, xOffset, 10, null);
            xOffset += primary.getWidth() + 10;
        }
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
