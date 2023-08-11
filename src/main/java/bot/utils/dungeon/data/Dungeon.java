package bot.utils.dungeon.data;

import bot.utils.dungeon.DungeonGenerator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@Getter
@ToString
public class Dungeon {

    private int[][] map;
    private DungeonGenerator.Size size;

    public Dungeon(int[][] map, DungeonGenerator.Size size){
        this.map = map;
        this.size = size;
    }

}
