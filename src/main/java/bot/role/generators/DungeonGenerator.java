package bot.role.generators;

import bot.role.data.jsonConfig.GameConfigValues;
import bot.role.dungeon.Room;
import bot.role.dungeon.astar.Node;
import bot.role.dungeon.graph.Edge;
import bot.role.dungeon.graph.Graph;
import bot.role.dungeon.graph.Vertex;
import data.serializing.DataCacher;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public abstract class DungeonGenerator {

    public enum Size {
        SMALL, MEDIUM, LARGE
    }

    public static int[][] GenerateDungeon(Size size){
        int map[][];
        List<Room> rooms;
        List<Vertex> roomCenters;

        rooms = new LinkedList<>();
        GameConfigValues gcv = new DataCacher<GameConfigValues>("game_config").loadSerialized();
        int dungeonRoomCount;
        switch(size){
            case LARGE:
                dungeonRoomCount = gcv.getLargeDungeonRoomCount();
                break;
            case MEDIUM:
                dungeonRoomCount = gcv.getMediumDungeonRoomCount();
                break;
            case SMALL:
                dungeonRoomCount = gcv.getSmallDungeonRoomCount();
                break;
            default:
                dungeonRoomCount = 5;
                break;
        }

        map = new int[dungeonRoomCount * 2][dungeonRoomCount * 2];

        for(int i = 0; i < 100; i++){
            generateRoom(rooms, map);
        }

        roomCenters = new LinkedList<>();
        for(Room room : rooms){
            roomCenters.add(new Vertex(room.getCenter()));
        }

        Graph graph = new Graph(roomCenters,dungeonRoomCount * 2, dungeonRoomCount * 2);

        List<Edge> edges = graph.getMinimumSpanningTree();
        for(int[] path : graph.getHallways()){
            for(int[] foundPath : pathFind(path, map)){
                if(map[foundPath[0]][foundPath[1]] == 0) {
                    map[foundPath[0]][foundPath[1]] = 4;
                }
            }
        }

        cleanUpDoors(rooms, map);
        try {
            saveDungeon(map);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return map;
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
        if(y > 0){
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

    public static void saveDungeon(int[][] map) throws IOException {
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

        BufferedImage tileSet = ImageIO.read(new File("tileset.png"));

        for(int x = 0; x < map.length; x++){
            int photoX = x * 20 + 20;
            for(int y = 0; y < map[0].length; y++){
                int photoY = y * 20 + 20;
                boolean topWall = y > 0 && map[x][y - 1] == 3;
                boolean rightWall = x < map.length - 1 && map[x + 1][y] == 3;
                boolean bottomWall = y < map[x].length - 1 && map[x][y + 1] == 3;
                boolean leftWall = x > 0 && map[x - 1][y] == 3;

                int tileSetOffset = -1;
                int tileSetBackground = -1;
                switch (map[x][y]){
                    case 0: // open tile
                        break;
                    case 1: // room tile
                        tileSetBackground = 2;
                       break;
                    case 2: // door
                        if(!topWall && !bottomWall && rightWall || leftWall){
                            tileSetOffset = 11;
                        } else if(topWall || bottomWall && !rightWall && !leftWall){
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
                        } else if (!topWall && !bottomWall && rightWall && !leftWall){
                            tileSetOffset = 2;
                            tileSetBackground = 0;
                        } else if (!topWall && bottomWall && !rightWall && !leftWall){
                            tileSetOffset = 3;
                            tileSetBackground = 0;
                        } else if (!topWall && !bottomWall && !rightWall && leftWall){
                            tileSetOffset = 4;
                            tileSetBackground = 0;
                        } else if (topWall && !bottomWall && !rightWall && !leftWall){
                            tileSetOffset = 5;
                            tileSetBackground = 0;
                        } else if (!topWall && bottomWall && rightWall && !leftWall){
                            tileSetOffset = 6;
                            tileSetBackground = 0;
                        } else if (!topWall && bottomWall && !rightWall && leftWall){
                            tileSetOffset = 7;
                            tileSetBackground = 0;
                        } else if (topWall && !bottomWall && !rightWall && leftWall){
                            tileSetOffset = 8;
                            tileSetBackground = 0;
                        } else if (topWall && !bottomWall && rightWall && !leftWall){
                            tileSetOffset = 9;
                            tileSetBackground = 0;
                        } else if (!topWall && !bottomWall && !rightWall && !leftWall){
                            tileSetOffset = 10;
                            tileSetBackground = 0;
                        }
                        break;
                    case 4: // hallway
                        tileSetBackground = 1;
                        break;
                }
                if(tileSetBackground != -1){
                    pane.drawImage(tileSet.getSubimage(0 * 20, (tileSetBackground + 1) * 20, 20, 20), photoX, photoY, null);
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
        ImageIO.write(bufferedImage, "png", file);

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

        while (open.size() > 0){
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

    /**
     * Gets the cost of a specific tile for a*. The lower the cost the more favorable a tile will be to traverse
     * @param tileType
     * @return
     */
    private static int getCost(int tileType){
        switch (tileType){
            case 1: // room floor tile
            case 2: // door tile
                return 1;
            case 0: // empty tile
                return 5;
            case 4: // existing hallway
                return 1;
            default:
                return 1000000;
        }
    }



}
