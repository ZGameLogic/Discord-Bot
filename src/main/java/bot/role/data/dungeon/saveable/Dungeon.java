package bot.role.data.dungeon.saveable;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import data.serializing.SavableData;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;
import java.util.List;

@NoArgsConstructor
@Getter
@ToString
public class Dungeon extends SavableData {

    private int[][] map;
    private List<Room> rooms;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date departs;

    public Dungeon(long id, int[][] map, List<Room> rooms, Date departs){
        super(id);
        this.map = map;
        this.rooms = rooms;
        this.departs = departs;
    }

    @JsonIgnore
    public double getEncounterAveragePerRoom(){
        double total = 0;
        for(Room room : rooms){
            if(room.getEncounters() != null){
                total += room.getEncounters().size();
            }
        }
        return total / rooms.size();
    }

    @JsonIgnore
    public int getClearedRoomCount(){
        int count = 0;
        for(Room room : rooms){
            if(room.isCleared()) count++;
        }
        return count;
    }

    @JsonIgnore
    public double getClearedRoomPercentage(){
        return getClearedRoomCount() / (double) rooms.size();
    }




}
