package controllers.pokemon;

import controllers.pokemon.structures.Pokemon;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.*;

public abstract class PokemonAPI {

    private static final String API_URL = "https://pokeapi.co/api/v2/";

    /**
     * Get a pokemon with information from a specific version
     * @param name Name of the pokemon
     * @param version version to look up details from version
     * @return a pokemon with information from a specific version
     */
    public static Optional<Pokemon> getByName(String name, String version){
        Pokemon pokemon = new Pokemon(getRequest(API_URL + "/pokemon/" + name.toLowerCase()), version);
        return Optional.of(pokemon);
    }

    public static Optional<String> getPokedexEntry(int pokedexEntry){
        try {
            JSONObject result = getRequest(API_URL + "/pokemon-species/" + pokedexEntry);
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
        Set<Pokemon.Type> weakTo = new HashSet<>();
        Set<Pokemon.Type> strongAgainst = new HashSet<>();
        for(Pokemon.Type type : types) {
            try {
                JSONObject result = getRequest(API_URL + "/type/" + type.name().toLowerCase());
                JSONArray doubleDamageFrom = result.getJSONObject("damage_relations").getJSONArray("double_damage_from");
                for(int i = 0; i < doubleDamageFrom.length(); i++){
                    weakTo.add(Pokemon.Type.fromString(doubleDamageFrom.getJSONObject(i).getString("name")));
                }
                JSONArray halfDamageFrom = result.getJSONObject("damage_relations").getJSONArray("half_damage_from");
                for(int i = 0; i < halfDamageFrom.length(); i++){
                    strongAgainst.add(Pokemon.Type.fromString(doubleDamageFrom.getJSONObject(i).getString("name")));
                }
            } catch (JSONException e) {

            }
        }
        List<Pokemon.Type> total = new LinkedList<>(weakTo);
        total.removeAll(strongAgainst);
        return total;
    }

    public static List<String> getVersions(){
        List<String> versions = new LinkedList<>();
        try {
            String url = API_URL + "/version?limit=50";
            while(!url.equals("null")) {
                JSONObject results = getRequest(url);
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

    private static JSONObject getRequest(String url){
        try {
            HttpClient httpclient = HttpClients.createDefault();
            URIBuilder builder = new URIBuilder(url);
            URI uri = builder.build();
            HttpGet request = new HttpGet(uri);
            HttpResponse response = httpclient.execute(request);
            return new JSONObject(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            return new JSONObject();
        }
    }
}
