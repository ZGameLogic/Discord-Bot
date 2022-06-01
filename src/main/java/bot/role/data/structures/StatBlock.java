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

    public String toString(){
        String returnThis = "";
        returnThis += magic > 0 ? "Magic increased by " + magic + ", " : "";
        returnThis += knowledge > 0 ? "Magic increased by " + knowledge + ", " : "";
        returnThis += stamina > 0 ? "Magic increased by " + stamina + ", " : "";
        returnThis += strength > 0 ? "Magic increased by " + strength + ", " : "";
        returnThis += agility > 0 ? "Magic increased by " + agility + ", " : "";
        return returnThis.substring(0, returnThis.length() - 2);
    }
}
