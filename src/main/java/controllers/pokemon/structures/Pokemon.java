package controllers.pokemon.structures;

import controllers.pokemon.PokemonAPI;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

@Getter
@ToString
public class Pokemon {
    private String name;
    private int pokedexId;
    private String pokedexEntry;
    private String sprite;
    private int baseHp;
    private int baseAttack;
    private int baseDefense;
    private int baseSpecialAttack;
    private int baseSpecialDefense;
    private int baseSpeed;
    private List<Type> types;
    private List<Type> weakTo;
    private List<Ability> abilities;
    private List<Move> moves;

    public Pokemon (JSONObject jsonObject, String version){
        try {
            name = jsonObject.getString("name");
            name = name.substring(0, 1).toUpperCase() + name.substring( 1);
            pokedexId = jsonObject.getInt("id");
            pokedexEntry = PokemonAPI.getPokedexEntry(pokedexId).orElse("Could not find pokedex entry");
            sprite = jsonObject.getJSONObject("sprites").getString("front_default");
            JSONArray stats = jsonObject.getJSONArray("stats");
            for(int i = 0; i < stats.length(); i++){
                JSONObject current = stats.getJSONObject(i);
                switch (current.getJSONObject("stat").getString("name")){
                    case "hp":
                        baseHp = current.getInt("base_stat");
                        break;
                    case "attack":
                        baseAttack = current.getInt("base_stat");
                        break;
                    case "defense":
                        baseDefense = current.getInt("base_stat");
                        break;
                    case "special-attack":
                        baseSpecialAttack = current.getInt("base_stat");
                        break;
                    case "special-defense":
                        baseSpecialDefense = current.getInt("base_stat");
                        break;
                    case "speed":
                        baseSpeed = current.getInt("base_stat");
                        break;
                }
            }
            types = new LinkedList<>();
            JSONArray jsonTypes = jsonObject.getJSONArray("types");
            for(int i = 0; i < jsonTypes.length(); i++){
                types.add(Type.fromString(jsonTypes.getJSONObject(i).getJSONObject("type").getString("name")));
            }
            weakTo = PokemonAPI.getWeakToTypes(types);
            abilities = new LinkedList<>();
            JSONArray jsonAbilities = jsonObject.getJSONArray("abilities");
            for(int i = 0; i < jsonAbilities.length(); i++){
                JSONObject currentAbility = jsonAbilities.getJSONObject(i);
                abilities.add(new Ability(currentAbility));
            }
            moves = new LinkedList<>();
            JSONArray jsonMoves = jsonObject.getJSONArray("moves");
            for(int i = 0; i < jsonMoves.length(); i++){
                moves.add(new Move(jsonMoves.getJSONObject(i)));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @AllArgsConstructor
    @Getter
    public enum Type {
        NORMAL(new Color(167, 167, 120)),
        FIRE(new Color(238, 127, 49)),
        WATER(new Color(103, 143, 239)),
        GRASS(new Color(119, 198, 80)),
        ELECTRIC(new Color(246, 206, 49)),
        ICE(new Color(151, 214, 215)),
        FIGHTING(new Color(190, 48, 41)),
        POISON(new Color(159, 63, 160)),
        GROUND(new Color(222, 190, 104)),
        FLYING(new Color(167, 143, 239)),
        PSYCHIC(new Color(246, 87, 136)),
        BUG(new Color(167, 183, 33)),
        ROCK(new Color(183, 159, 57)),
        GHOST(new Color(111, 87, 152)),
        DARK(new Color(111, 87, 72)),
        DRAGON(new Color(111, 56, 247)),
        STEEL(new Color(183, 183, 207)),
        FAIRY(new Color(236, 152, 172));

        private Color color;

        public static Type fromString(String type){
            for(Type t : Type.values()){
                if(t.name().toLowerCase().equals(type)){
                    return t;
                }
            }
            return null;
        }

        public String toString(){
            String name = name();
            return name.substring(0, 1) + name.substring(1).toLowerCase();
        }
    }

    @Getter
    @AllArgsConstructor
    @ToString
    public class Ability {
        private String name;
        private boolean hidden;

        public Ability(JSONObject jsonObject) throws JSONException {
            name = jsonObject.getJSONObject("ability").getString("name").replace('-', ' ');
            hidden = jsonObject.getBoolean("is_hidden");
        }
    }

    @Getter
    @AllArgsConstructor
    @ToString
    public class Move {
        private String name;

        public Move(JSONObject jsonObject) throws JSONException {
            name = jsonObject.getJSONObject("move").getString("name").replace('-', ' ');
        }
    }
}
