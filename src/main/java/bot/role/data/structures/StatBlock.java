package bot.role.data.structures;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class StatBlock {
    private int magic, knowledge, stamina, strength, agility;
}
