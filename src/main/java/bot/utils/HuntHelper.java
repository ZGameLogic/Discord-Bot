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

import java.awt.*;
import java.util.Collections;
import java.util.LinkedList;

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
        eb.clearFields();
        eb.addField("Loadout", loadout.toString(), false);
        return eb.build();
    }

    public static HuntLoadout generateLoadout(boolean dualWield, boolean quartermaster, boolean specialAmmo, boolean medkitMelee, HuntItemRepository huntItemRepository, HuntGunRepository huntGunRepository){
        // guns
        int gunBudget = quartermaster ? 5 : 4;
        // secondary fist
        LinkedList<HuntGun> mediumSmallGuns = huntGunRepository.findAllMediumAndSmallGuns();
        if(dualWield){ // add duals if the user wants them in
            LinkedList<HuntGun> duals = huntGunRepository.findAllDuals();
            duals.forEach(gun -> gun.setSlot(HuntGun.Slot.MEDIUM));
            mediumSmallGuns.addAll(duals);
        }
        Collections.shuffle(mediumSmallGuns);
        HuntGun secondary = mediumSmallGuns.getFirst();

        gunBudget -= secondary.getSlot() == HuntGun.Slot.MEDIUM ? 2 : 1;
        // primary second
        LinkedList<HuntGun> primaryPool = gunBudget == 3 ? huntGunRepository.findAllLargeGuns() : huntGunRepository.findAllMediumAndSmallGuns();
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
            for(int i = 0; i < primary.getSpecialAmmoCount(); i++){
                LinkedList<AmmoType> ammoPool =
                        !primary.hasSecondaryAmmo() ? primary.getAmmoTypes() :
                                (i == 0 ? primary.primaryAmmo() : primary.secondaryAmmo());
                Collections.shuffle(ammoPool);
                primaryAmmo.add(ammoPool.get(0));
            }
            for(int i = 0; i < secondary.getSpecialAmmoCount(); i++){
                LinkedList<AmmoType> ammoPool =
                        !secondary.hasSecondaryAmmo() ? secondary.getAmmoTypes() :
                                (i == 0 ? secondary.primaryAmmo() : secondary.secondaryAmmo());
                Collections.shuffle(ammoPool);
                secondaryAmmo.add(ammoPool.get(0));
            }
        } else {
            for(int i = 0; i < primary.getSpecialAmmoCount(); i++){
                primaryAmmo.add(!primary.hasSecondaryAmmo() ? primary.getDefaultAmmo() : (i == 0 ? primary.getDefaultAmmo(false) : primary.getDefaultAmmo(true)));
            }
            for(int i = 0; i < secondary.getSpecialAmmoCount(); i++){
                secondaryAmmo.add(secondary.getDefaultAmmo());
            }
        }

        // tools
        LinkedList<HuntItem> tools = new LinkedList<>();
        if(medkitMelee){
            tools.add(huntItemRepository.findById("Medkit").get());
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

//    public static File saveDungeon(Dungeon dungeon) {
//        int[][] map = dungeon.getMap();
//        int width = map.length * 20 + 40;
//        int height = map[0].length * 20 + 40;
//
//        // Constructs a BufferedImage of one of the predefined image types.
//        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//
//        // Create a graphics which can be used to draw into the buffered image
//        Graphics2D pane = bufferedImage.createGraphics();
//
//        // background
//        pane.setColor(new Color(88, 81, 88));
//        pane.fillRect(0, 0, width, height);
//
//        // switch back to black
//        pane.setColor(Color.black);
//
//        BufferedImage tileSet = null;
//        try {
//            tileSet = ImageIO.read(DungeonGenerator.class.getClassLoader().getResourceAsStream("Role bot/Dungeons/tileset.png"));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        for(int x = 0; x < map.length; x++){
//            int photoX = x * 20 + 20;
//            for(int y = 0; y < map[0].length; y++){
//                int photoY = y * 20 + 20;
//
//                boolean topWall = y > 0 && map[x][y - 1] == 3;
//                boolean rightWall = x < map.length - 1 && map[x + 1][y] == 3;
//                boolean bottomWall = y < map[x].length - 1 && map[x][y + 1] == 3;
//                boolean leftWall = x > 0 && map[x - 1][y] == 3;
//
//                boolean topDoor = getUpTile(x, y, map) == 2;
//                boolean rightDoor = getRightTile(x, y, map) == 2;
//                boolean bottomDoor = getDownTile(x, y, map) == 2;
//                boolean leftDoor = getLeftTile(x, y, map) == 2;
//
//                boolean topPath = getUpTile(x, y, map) == 4 || topDoor;
//                boolean rightPath = getRightTile(x, y, map) == 4 || rightDoor;
//                boolean bottomPath = getDownTile(x, y, map) == 4 || bottomDoor;
//                boolean leftPath = getLeftTile(x, y, map) == 4 || leftDoor;
//
//
//                int tileSetOffset = -1;
//                int tileSetBackground = -1;
//                int tileSetForeground = -1;
//
//                switch (map[x][y]){
//                    case 0: // open tile
//                        break;
//                    case 1: // room tile
//                        tileSetBackground = 2;
//                        break;
//                    case 2: // door
//                        if(!topWall && !bottomWall && rightWall || leftWall){
//                            tileSetOffset = 11;
//                        } else if(topWall || bottomWall && !rightWall && !leftWall){
//                            tileSetOffset = 12;
//                        }
//                        tileSetBackground = 2;
//                        break;
//                    case 3: // wall
//                        if(!topWall && !bottomWall && rightWall && leftWall){
//                            tileSetOffset = 0;
//                            tileSetBackground = 0;
//                        } else if(topWall && bottomWall && !rightWall && !leftWall){
//                            tileSetOffset = 1;
//                            tileSetBackground = 0;
//                        } else if (!topWall && !bottomWall && rightWall && !leftWall){
//                            tileSetOffset = 2;
//                            tileSetBackground = 0;
//                        } else if (!topWall && bottomWall && !rightWall && !leftWall){
//                            tileSetOffset = 3;
//                            tileSetBackground = 0;
//                        } else if (!topWall && !bottomWall && !rightWall && leftWall){
//                            tileSetOffset = 4;
//                            tileSetBackground = 0;
//                        } else if (topWall && !bottomWall && !rightWall && !leftWall){
//                            tileSetOffset = 5;
//                            tileSetBackground = 0;
//                        } else if (!topWall && bottomWall && rightWall && !leftWall){
//                            tileSetOffset = 6;
//                            tileSetBackground = 0;
//                        } else if (!topWall && bottomWall && !rightWall && leftWall){
//                            tileSetOffset = 7;
//                            tileSetBackground = 0;
//                        } else if (topWall && !bottomWall && !rightWall && leftWall){
//                            tileSetOffset = 8;
//                            tileSetBackground = 0;
//                        } else if (topWall && !bottomWall && rightWall && !leftWall){
//                            tileSetOffset = 9;
//                            tileSetBackground = 0;
//                        } else if (!topWall && !bottomWall && !rightWall && !leftWall){
//                            tileSetOffset = 10;
//                            tileSetBackground = 0;
//                        }
//                        break;
//                    case 4: // hallway
//                        tileSetBackground = 1;
//
//                        if(topPath && rightPath && bottomPath && leftPath){ // all path
//                            tileSetOffset = 17;
//                        } else if(topPath && rightPath && bottomPath && !leftPath) { // no path left
//                            tileSetOffset = 18;
//                        } else if(topPath && !rightPath && bottomPath && leftPath) { // no path right
//                            tileSetOffset = 20;
//                        } else if(!topPath && rightPath && bottomPath && leftPath) { // no path top
//                            tileSetOffset = 19;
//                        } else if(topPath && rightPath && !bottomPath && leftPath) { // no path bottom
//                            tileSetOffset = 21;
//                        } else if(topPath && !rightPath && bottomPath && !leftPath) { // no path left or right
//                            tileSetOffset = 1;
//                        } else if(!topPath && rightPath && !bottomPath && leftPath) { // no path bottom or top
//                            tileSetOffset = 0;
//                        } else if(topPath && rightPath && !bottomPath && !leftPath) { // no path bottom or left
//                            tileSetOffset = 9;
//                        } else if(topPath && !rightPath && !bottomPath && leftPath) { // no path bottom or right
//                            tileSetOffset = 8;
//                        } else if(!topPath && rightPath && bottomPath && !leftPath) { // no path top or left
//                            tileSetOffset = 6;
//                        } else if(!topPath && !rightPath && bottomPath && leftPath) { // no path top or right
//                            tileSetOffset = 7;
//                        }
//                        if(topDoor && bottomDoor){
//                            tileSetForeground = 5;
//                        } else if(leftDoor && rightDoor) {
//                            tileSetForeground = 4;
//                        } else if(topDoor){
//                            tileSetForeground = 0;
//                        } else if (rightDoor){
//                            tileSetForeground = 1;
//                        } else if (bottomDoor){
//                            tileSetForeground = 2;
//                        } else if (leftDoor){
//                            tileSetForeground = 3;
//                        }
//                        break;
//                }
//                if(tileSetBackground != -1){
//                    Random random = new Random();
//                    if(random.nextInt(100) < 5) {
//                        int tileVariations = 2;
//                        pane.drawImage(tileSet.getSubimage((random.nextInt(tileVariations) + 1) * 20, (tileSetBackground + 1) * 20, 20, 20), photoX, photoY, null);
//                    } else {
//                        pane.drawImage(tileSet.getSubimage(0, (tileSetBackground + 1) * 20, 20, 20), photoX, photoY, null);
//                    }
//                }
//                if(tileSetForeground != -1){
//                    pane.drawImage(tileSet.getSubimage(22 * 20 + tileSetForeground * 20, 0, 20, 20), photoX, photoY, null);
//                }
//                if(tileSetOffset != -1) {
//                    pane.drawImage(tileSet.getSubimage(tileSetOffset * 20, 0, 20, 20), photoX, photoY, null);
//                }
//
//            }
//        }
//        // Disposes of this graphics context and releases any system resources that it is using.
//        pane.dispose();
//
//        // Save as PNG
//        File file = new File("arena//dungeon photos//dungeon.png");
//        try {
//            file.mkdirs();
//            file.createNewFile();
//            ImageIO.write(bufferedImage, "png", file);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        return file;
//    }
