package controllers.pokemon;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

public abstract class PokemonAPI {

    private static final String API_URL = "https://pokeapi.co/api/v2/";
    private static List<String> pokemonNames;

    public static void getByName(String name) throws URISyntaxException, IOException {
        HttpClient httpclient = HttpClients.createDefault();
        URIBuilder builder = new URIBuilder(API_URL + "/pokemon/" + name.toLowerCase());
        URI uri = builder.build();
        HttpGet request = new HttpGet(uri);
        HttpResponse response = httpclient.execute(request);
        System.out.println(EntityUtils.toString(response.getEntity()));
    }

    public static void updateAllNames() {
        try {
            String next = API_URL + "/pokemon?limit=500";
            List<String> names = new LinkedList<>();
            while (!next.equalsIgnoreCase("null")) {
                HttpClient httpclient = HttpClients.createDefault();
                URIBuilder builder = new URIBuilder(next);
                URI uri = builder.build();
                HttpGet request = new HttpGet(uri);
                HttpResponse response = httpclient.execute(request);
                JSONObject results = new JSONObject(EntityUtils.toString(response.getEntity()));
                JSONArray resultsArray = results.getJSONArray("results");
                for (int i = 0; i < resultsArray.length(); i++) {
                    names.add(resultsArray.getJSONObject(i).getString("name"));
                }
                next = results.getString("next");
            }
            pokemonNames = names;
        } catch (Exception e){

        }
    }

    /**
     * Checks if the name given is a valid pokemon name.
     * Automatically calls updateAllNames to update the name list if it is empty.
     *
     * @See updateAllNames
     * @param name Name to be checked
     * @return true if the name exists as a pokemon
     */
    public static boolean pokemonExists(String name){
        if(pokemonNames == null) updateAllNames();
        if(pokemonNames == null) return false;
        return pokemonNames.contains(name.toLowerCase());
    }

    /**
     * Returns a list of names that you could have meant when typing
     * @param name Name to check with
     * @return A list of possible names
     */
    public static List<String> closeNames(String name){
        if(pokemonNames == null) updateAllNames();
        if(pokemonNames == null) return new LinkedList<>();
        name = name.toLowerCase();
        List<NameScore> nameScoreList = new LinkedList<>();
        for(String pokemonName: pokemonNames) {
            int score = 0;
            for (int i = 0; i < (name.length() < pokemonName.length() ? name.length() : pokemonName.length()); i++) {
                if (name.charAt(i) == pokemonName.charAt(i)) {
                    score++;
                }
            }
            nameScoreList.add(new NameScore(pokemonName, score));
        }
        Collections.sort(nameScoreList);
        List<String> names = new LinkedList<>();
        for(NameScore ns : nameScoreList.subList(0, 20)){
            if(ns.getScore() / (double)name.length() >= .5){
                names.add(ns.getName());
            }
        }
        return names;
    }

    @AllArgsConstructor
    @Getter
    private static class NameScore implements Comparable<NameScore>{
        private String name;
        private int score;

        @Override
        public int compareTo(@NotNull PokemonAPI.NameScore o) {
            return Integer.compare(o.score, score);
        }
    }
}
