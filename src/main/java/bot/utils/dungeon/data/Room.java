package bot.utils.dungeon.data;

import lombok.Getter;

import java.util.LinkedList;
import java.util.List;

@Getter
public class Room {
    private int x;
    private int y;
    private int width;
    private int height;
    private List<int[]> doors;

    public Room(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        doors = new LinkedList<>();
    }

    public double[] getCenter(){
        return new double[] {
                width / 2.0 + x,
                height / 2.0 + y
        };
    }

    public List<int[]> getDoorsAbsolute(){
        List<int[]> absolute = new LinkedList<>();
        for(int[] door : doors){
            absolute.add(new int[] {door[0] + x, door[1] + y});
        }
        return absolute;
    }

    public void addDoor(int x, int y){
        doors.add(new int[] {x, y});
    }
}