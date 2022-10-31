package bot.role.helpers.roleData;

import lombok.Getter;

import java.awt.Color;
import java.util.*;

@Getter
public abstract class RoleDataRepository {

    private static final Role KING = new Role("King", "King/Queen", "kings", new Color(239, 194, 16));
    private static final Role DUKE = new Role("Duke", "Contemptible Duke", "dukes", new Color(162, 65, 3));
    private static final Role BARON = new Role("Baron", "Irrelevant Baron", "barons", new Color(35, 80, 152));
    private static final Role LORD = new Role("Lord", "Ignoble Lord", "lords", new Color(52, 146, 210));
    private static final Role KNIGHT = new Role("Knight", "Cowardly Knight", "knights", new Color(162, 38, 39));
    private static final Role SQUIRE = new Role("Squire", "Apprehensive Squire", "squires", new Color(107, 27, 218));
    private static final Role CRAFTSMEN = new Role("Craftsmen", "Unproductive Craftsmen", "craftsmen", new Color(31, 138, 76));
    private static final Role FARMER = new Role("Farmer", "Profitless Farmer", "farmers", new Color(81, 72, 46));

    public static List<Role> getRoles(){
        List<Role> r = new LinkedList<>();
        r.addAll(Arrays.asList(new Role[]{
                KING, DUKE, BARON, LORD, KNIGHT, SQUIRE, CRAFTSMEN, FARMER
        }));
        return r;
    }

    public static Map<String, String> getRoleMap(){
        Map<String, String> map = new HashMap<>();
        for(Role role : getRoles()){
            map.put(role.getLongName(), role.getShortName());
        }
        return map;
    }

}
