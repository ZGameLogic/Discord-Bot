package bot.role.helpers.roleData;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.awt.*;

@AllArgsConstructor
@Getter
public class Role {
    private String shortName, longName, plural;
    private Color color;
}
