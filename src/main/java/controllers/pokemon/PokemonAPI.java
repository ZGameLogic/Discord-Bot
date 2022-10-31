package controllers.pokemon;

import controllers.network.Network;
import controllers.pokemon.structures.Pokemon;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public abstract class PokemonAPI {

    private static final String API_URL = "https://pokeapi.co/api/v2/";

    /**
     * Get a pokemon with information from a specific version
     * @param name Name of the pokemon
     * @return a pokemon with information from a specific version
     */
    public static Optional<Pokemon> getByName(String name){
        JSONObject pokemonJSON = Network.get(API_URL + "/pokemon/" + name.toLowerCase());
        if(pokemonJSON != null) {
            Pokemon pokemon = new Pokemon(pokemonJSON);
            return Optional.of(pokemon);
        }
        return Optional.empty();
    }

    public static Optional<String> getPokedexEntry(int pokedexEntry){
        try {
            JSONObject result = Network.get(API_URL + "/pokemon-species/" + pokedexEntry);
            JSONArray flavorTextEntries = result.getJSONArray("flavor_text_entries");
            int tries = 0;
            while(tries < 1000){
                JSONObject entry = flavorTextEntries.getJSONObject(new Random().nextInt(flavorTextEntries.length()));
                if(entry.getJSONObject("language").getString("name").equals("en")) {
                    return Optional.of(entry.getString("flavor_text").replace('\n', ' ').replace('\f', ' '));
                }
            }
            return Optional.empty();
        } catch (JSONException e){
            return Optional.empty();
        }
    }

    public static List<Pokemon.Type> getWeakToTypes(Collection<Pokemon.Type> types){
        return getWeakToTypes(types.toArray(new Pokemon.Type[types.size()]));
    }

    public static List<Pokemon.Type> getWeakToTypes(Pokemon.Type...types){
        List<Pokemon.Type> typeList = new LinkedList<>();
        for(Pokemon.Type type : types) {
            try {
                JSONObject result = Network.get(API_URL + "/type/" + type.name().toLowerCase());
                JSONArray doubleDamageFromJSON = result.getJSONObject("damage_relations").getJSONArray("double_damage_from");
                JSONArray halfDamageFromJSON = result.getJSONObject("damage_relations").getJSONArray("half_damage_from");
                for(int i = 0; i < doubleDamageFromJSON.length(); i++){
                    typeList.add(Pokemon.Type.fromString(doubleDamageFromJSON.getJSONObject(i).getString("name")));
                }
                for(int i = 0; i < halfDamageFromJSON.length(); i++){
                    typeList.remove(Pokemon.Type.fromString(halfDamageFromJSON.getJSONObject(i).getString("name")));
                }

            } catch (JSONException e) {

            }
        }
        return new LinkedList<>(new HashSet<>(typeList));
    }

    public static List<String> getVersions(){
        List<String> versions = new LinkedList<>();
        try {
            String url = API_URL + "/version?limit=50";
            while(!url.equals("null")) {
                JSONObject results = Network.get(url);
                JSONArray names = results.getJSONArray("results");
                for(int i = 0; i < names.length(); i++){
                    versions.add(names.getJSONObject(i).getString("name").replace('-', ' '));
                }
                url = results.getString("next");
            }
        } catch (JSONException e){

        }
        return versions;
    }
}
