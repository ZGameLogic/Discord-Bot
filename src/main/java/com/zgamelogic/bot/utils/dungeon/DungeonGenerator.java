package com.zgamelogic.bot.utils.dungeon;
import com.zgamelogic.bot.utils.dungeon.data.Node;
import com.zgamelogic.bot.utils.dungeon.data.Room;
import com.zgamelogic.bot.utils.dungeon.data.graph.Graph;
import com.zgamelogic.bot.utils.dungeon.data.graph.Vertex;
import com.zgamelogic.bot.utils.dungeon.data.Dungeon;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public abstract class DungeonGenerator {

    public enum Size {
        SMALL, MEDIUM, LARGE;
        public static Size random(){
            return Size.values()[new Random().nextInt(Size.values().length)];
        }
    }

    public static Dungeon GenerateRandomDungeon(){
        return GenerateDungeon(Size.random());
    }

    public static Dungeon GenerateDungeon(Size size){
        int[][] map;
        List<Room> rooms;
        List<Vertex> roomCenters;

        rooms = new LinkedList<>();
        int dungeonRoomCount = switch (size) {
            case LARGE -> 30;
            case MEDIUM -> 20;
            case SMALL -> 10;
        };

        map = new int[dungeonRoomCount * 2][dungeonRoomCount * 2];

        for(int i = 0; i < 100; i++){
            generateRoom(rooms, map);
        }

        roomCenters = new LinkedList<>();
        for(Room room : rooms){
            roomCenters.add(new Vertex(room.getCenter()));
        }

        Graph graph = new Graph(roomCenters,dungeonRoomCount * 2, dungeonRoomCount * 2);

        for(int[] path : graph.getHallways()){
            for(int[] foundPath : pathFind(path, map)){
                if(map[foundPath[0]][foundPath[1]] == 0) {
                    map[foundPath[0]][foundPath[1]] = 4;
                }
            }
        }

        cleanUpDoors(rooms, map);

        return new Dungeon(map, size);
    }

    private static void cleanUpDoors(List<Room> rooms, int[][] map) {
        for(Room room : rooms){
            for(int[] door : room.getDoorsAbsolute()){
                int doorX =  door[0];
                int doorY = door[1];
                if(getUpTile(doorX, doorY, map) != 4 &&
                        getRightTile(doorX, doorY, map) != 4 &&
                        getDownTile(doorX, doorY, map) != 4 &&
                        getLeftTile(doorX, doorY, map) != 4){
                    map[doorX][doorY] = 3;
                }
            }
        }
    }

    private static int getUpTile(int x, int y, int[][] map){
        if(y > 0 && x < map.length){
            return map[x][y - 1];
        }
        return -1;
    }

    private static int getRightTile(int x, int y, int[][] map){
        if(x < map.length - 1){
            return map[x + 1][y];
        }
        return -1;
    }

    private static int getDownTile(int x, int y, int[][] map){
        if(y < map[x].length - 1){
            return map[x][y + 1];
        }
        return -1;
    }

    private static int getLeftTile(int x, int y, int[][] map){
        if(x > 0){
            return map[x - 1][y];
        }
        return -1;
    }

    /**
     * Places a random room on the grid
     */
    private static void generateRoom(List<Room> rooms, int[][] map) {
        Random random = new Random();
        boolean placed = false;
        int totalRetries = 0;
        while(!placed && totalRetries < 20){
            int width = random.nextInt(7) + 4; // random int between 4 and 10
            int height = random.nextInt(7) + 4; // random int between 4 and 10
            int x = random.nextInt(map.length - width - 1) + 1;
            int y = random.nextInt(map[0].length - height - 1) + 1;
            Room room = new Room(x, y, width, height);
            int triesWithCurrentSize = 0;
            while(!placed && triesWithCurrentSize < 20){
                // Check for valid placement
                if(checkForValidPlacement(room, map)){
                    addDoorsToRoom(room, random.nextInt(2) + 1);
                    placeRoom(room, rooms, map);
                    placed = true;
                }
                triesWithCurrentSize++;
            }
            totalRetries++;
        }
    }

    private static boolean checkForValidPlacement(Room room, int[][] map){
        int startX = room.getX() > 1 ? room.getX() - 1 : 0;
        int startY = room.getY() > 1 ? room.getY() - 1 : 0;
        for(int i = startX; i < map.length && i < room.getX() + room.getWidth() + 1; i++){
            for(int j = startY; j < map[i].length && j < room.getY() + room.getHeight() + 1; j++){
                if(map[i][j] == 1 || map[i][j] == 2 || map[i][j] == 3){
                    return false;
                }
            }
        }
        return true;
    }

    private static void placeRoom(Room room, List<Room> rooms, int[][] map){
        rooms.add(room);
        for(int i = room.getX(); i < map.length && i < room.getX() + room.getWidth(); i++){
            for(int j = room.getY(); j < map[i].length && j < room.getY() + room.getHeight(); j++){
                if(i == room.getX() || i == room.getX() + room.getWidth() - 1 ||
                        j == room.getY() || j == room.getY() + room.getHeight() - 1){
                    map[i][j] = 3;
                } else {
                    map[i][j] = 1;
                }
            }
        }

        for(int[] cords : room.getDoors()){
            map[room.getX() + cords[0]][room.getY() + cords[1]] = 2;
        }
    }

    public static File saveDungeon(Dungeon dungeon) {
        int[][] map = dungeon.getMap();
        int width = map.length * 20 + 40;
        int height = map[0].length * 20 + 40;

        // Constructs a BufferedImage of one of the predefined image types.
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Create a graphics which can be used to draw into the buffered image
        Graphics2D pane = bufferedImage.createGraphics();

        // background
        pane.setColor(new Color(88, 81, 88));
        pane.fillRect(0, 0, width, height);

        // switch back to black
        pane.setColor(Color.black);

        BufferedImage tileSet;
        try {
            tileSet = ImageIO.read(DungeonGenerator.class.getClassLoader().getResourceAsStream("assets/Dungeons/tileset.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for(int x = 0; x < map.length; x++){
            int photoX = x * 20 + 20;
            for(int y = 0; y < map[0].length; y++){
                int photoY = y * 20 + 20;

                boolean topWall = y > 0 && map[x][y - 1] == 3;
                boolean rightWall = x < map.length - 1 && map[x + 1][y] == 3;
                boolean bottomWall = y < map[x].length - 1 && map[x][y + 1] == 3;
                boolean leftWall = x > 0 && map[x - 1][y] == 3;

                boolean topDoor = getUpTile(x, y, map) == 2;
                boolean rightDoor = getRightTile(x, y, map) == 2;
                boolean bottomDoor = getDownTile(x, y, map) == 2;
                boolean leftDoor = getLeftTile(x, y, map) == 2;

                boolean topPath = getUpTile(x, y, map) == 4 || topDoor;
                boolean rightPath = getRightTile(x, y, map) == 4 || rightDoor;
                boolean bottomPath = getDownTile(x, y, map) == 4 || bottomDoor;
                boolean leftPath = getLeftTile(x, y, map) == 4 || leftDoor;


                int tileSetOffset = -1;
                int tileSetBackground = -1;
                int tileSetForeground = -1;

                switch (map[x][y]){
                    case 0: // open tile
                        break;
                    case 1: // room tile
                        tileSetBackground = 2;
                        break;
                    case 2: // door
                        if(!topWall && !bottomWall && rightWall || leftWall){
                            tileSetOffset = 11;
                        } else if(topWall || bottomWall && !rightWall){
                            tileSetOffset = 12;
                        }
                        tileSetBackground = 2;
                        break;
                    case 3: // wall
                        if(!topWall && !bottomWall && rightWall && leftWall){
                            tileSetOffset = 0;
                            tileSetBackground = 0;
                        } else if(topWall && bottomWall && !rightWall && !leftWall){
                            tileSetOffset = 1;
                            tileSetBackground = 0;
                        } else if (!topWall && !bottomWall && rightWall){
                            tileSetOffset = 2;
                            tileSetBackground = 0;
                        } else if (!topWall && bottomWall && !rightWall && !leftWall){
                            tileSetOffset = 3;
                            tileSetBackground = 0;
                        } else if (!topWall && !bottomWall && leftWall){
                            tileSetOffset = 4;
                            tileSetBackground = 0;
                        } else if (topWall && !bottomWall && !rightWall && !leftWall){
                            tileSetOffset = 5;
                            tileSetBackground = 0;
                        } else if (!topWall && bottomWall && rightWall && !leftWall){
                            tileSetOffset = 6;
                            tileSetBackground = 0;
                        } else if (!topWall && bottomWall && !rightWall){
                            tileSetOffset = 7;
                            tileSetBackground = 0;
                        } else if (topWall && !bottomWall && !rightWall){
                            tileSetOffset = 8;
                            tileSetBackground = 0;
                        } else if (topWall && !bottomWall && !leftWall){
                            tileSetOffset = 9;
                            tileSetBackground = 0;
                        } else if (!topWall && !bottomWall){
                            tileSetOffset = 10;
                            tileSetBackground = 0;
                        }
                        break;
                    case 4: // hallway
                        tileSetBackground = 1;

                        if(topPath && rightPath && bottomPath && leftPath){ // all path
                            tileSetOffset = 17;
                        } else if(topPath && rightPath && bottomPath) { // no path left
                            tileSetOffset = 18;
                        } else if(topPath && !rightPath && bottomPath && leftPath) { // no path right
                            tileSetOffset = 20;
                        } else if(!topPath && rightPath && bottomPath && leftPath) { // no path top
                            tileSetOffset = 19;
                        } else if(topPath && rightPath && leftPath) { // no path bottom
                            tileSetOffset = 21;
                        } else if(topPath && !rightPath && bottomPath) { // no path left or right
                            tileSetOffset = 1;
                        } else if(!topPath && rightPath && !bottomPath && leftPath) { // no path bottom or top
                            tileSetOffset = 0;
                        } else if(topPath && rightPath) { // no path bottom or left
                            tileSetOffset = 9;
                        } else if(topPath && leftPath) { // no path bottom or right
                            tileSetOffset = 8;
                        } else if(!topPath && rightPath && bottomPath) { // no path top or left
                            tileSetOffset = 6;
                        } else if(!topPath && !rightPath && bottomPath && leftPath) { // no path top or right
                            tileSetOffset = 7;
                        }
                        if(topDoor && bottomDoor){
                            tileSetForeground = 5;
                        } else if(leftDoor && rightDoor) {
                            tileSetForeground = 4;
                        } else if(topDoor){
                            tileSetForeground = 0;
                        } else if (rightDoor){
                            tileSetForeground = 1;
                        } else if (bottomDoor){
                            tileSetForeground = 2;
                        } else if (leftDoor){
                            tileSetForeground = 3;
                        }
                        break;
                }
                if(tileSetBackground != -1){
                    Random random = new Random();
                    if(random.nextInt(100) < 5) {
                        int tileVariations = 2;
                        pane.drawImage(tileSet.getSubimage((random.nextInt(tileVariations) + 1) * 20, (tileSetBackground + 1) * 20, 20, 20), photoX, photoY, null);
                    } else {
                        pane.drawImage(tileSet.getSubimage(0, (tileSetBackground + 1) * 20, 20, 20), photoX, photoY, null);
                    }
                }
                if(tileSetForeground != -1){
                    pane.drawImage(tileSet.getSubimage(22 * 20 + tileSetForeground * 20, 0, 20, 20), photoX, photoY, null);
                }
                if(tileSetOffset != -1) {
                    pane.drawImage(tileSet.getSubimage(tileSetOffset * 20, 0, 20, 20), photoX, photoY, null);
                }

            }
        }
        // Disposes of this graphics context and releases any system resources that it is using.
        pane.dispose();

        // Save as PNG
        File file = new File("dungeon.png");
        try {
            file.mkdirs();
            file.createNewFile();
            ImageIO.write(bufferedImage, "png", file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }

    private static void addDoorsToRoom(Room room, int count){
        Random random = new Random();
        for(int i = 0; i < count; i++) {
            switch (random.nextInt(4)) {
                case 0: // top
                    room.addDoor(random.nextInt(room.getWidth() - 2) + 1, 0);
                    break;
                case 1: // right
                    room.addDoor(room.getWidth() - 1, random.nextInt(room.getHeight() - 2) + 1);
                    break;
                case 2: // bottom
                    room.addDoor(random.nextInt(room.getWidth() - 2) + 1, room.getHeight() - 1);
                    break;
                case 3: // left
                    room.addDoor(0, random.nextInt(room.getHeight() - 2) + 1);
                    break;
            }
        }
    }

    private static List<int[]> pathFind(int[] pathArray, int[][] map){
        Node.endNode = new Node(pathArray[2], pathArray[3]);
        Node startNode = new Node(pathArray[0], pathArray[1]);

        Queue<Node> open = new LinkedList<>();
        open.add(startNode);

        List<int[]> path = new LinkedList<>();

        while (!open.isEmpty()){
            Node current = open.remove();
            // Base case
            if(current.equals(Node.endNode)){
                // create path and return
                while(current.getParent() != null){
                    path.add(new int[] {current.getX(), current.getY()});
                    current = current.getParent();
                }
                return path;
            }
            // if we are not at the end
            List<Node> friends = new LinkedList<>();
            // top node
            if(current.getY() > 0 && map[current.getX()][current.getY() - 1] != 3){
                int tileType = map[current.getX()][current.getY() - 1];
                friends.add(new Node(current.getX(), current.getY() - 1, current, getCost(tileType)));
            }

            // right node
            if(current.getX() < map.length - 1 && map[current.getX() + 1][current.getY()] != 3){
                int tileType = map[current.getX() + 1][current.getY()];
                friends.add(new Node(current.getX() + 1, current.getY(), current, getCost(tileType)));
            }

            // down node
            if(current.getY() < map[0].length - 1 && map[current.getX()][current.getY() + 1] != 3){
                int tileType = map[current.getX()][current.getY() + 1];
                friends.add(new Node(current.getX(), current.getY() + 1, current, getCost(tileType)));
            }

            // left node
            if(current.getX() > 0 && map[current.getX() - 1][current.getY()] != 3){
                int tileType = map[current.getX() - 1][current.getY()];
                friends.add(new Node(current.getX() - 1, current.getY(), current, getCost(tileType)));
            }
            for(Node node : friends){
                if (node != null && !open.contains(node)) {
                    open.add(node);
                }
            }
        }
        return null;
    }

    private static int getCost(int tileType){
        return switch (tileType) { // room floor tile
            case 1, 2 -> // door tile
                    1;
            case 0 -> // empty tile
                    10;
            case 4 -> // existing hallway
                    1;
            default -> 1000000;
        };
    }



}
