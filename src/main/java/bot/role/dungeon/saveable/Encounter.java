package bot.role.dungeon.saveable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Encounter {
    private int magic, knowledge, strength, stamina, agility;
    private int goldReward;
}
