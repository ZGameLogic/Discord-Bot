package bot.role.dungeon.saveable;

import bot.role.data.item.Item;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class Room {
    private int x, y;
    private List<Encounter> encounters;
    private Map<Item.Material, Integer> materials;
    private int gold;
    private boolean cleared;

    public Room(bot.role.dungeon.Room room, List<Encounter> encounters, Map<Item.Material, Integer> materials, int gold){
        x = room.getX();
        y = room.getY();
        this.encounters = encounters;
        this.materials = materials;
        this.gold = gold;
        cleared = false;
    }
}
