package bot.listeners;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zgamelogic.AdvancedListenerAdapter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class TownsBot extends AdvancedListenerAdapter {

    private List<Item> items;
    private List<Recipe> recipes;
    private List<Building> buildings;

    public TownsBot() {
        super();
        try {
            ObjectMapper mapper = new ObjectMapper();
            URL itemFile = getClass().getClassLoader().getResource("Towns/Items.json");
            URL buildingFile = getClass().getClassLoader().getResource("Towns/Buildings.json");
            URL recipeFile = getClass().getClassLoader().getResource("Towns/Recipes.json");
            items = Arrays.asList(mapper.readValue(
                    itemFile, Item[].class
            ));
            recipes = Arrays.asList(mapper.readValue(
                    recipeFile, Recipe[].class
            ));
            buildings = Arrays.asList(mapper.readValue(
                    buildingFile, Building[].class
            ));
            log.info(recipes.toString());
        } catch (IOException e) {
            log.error("Unable to load json files for towns", e);
        }
    }


    @NoArgsConstructor
    @Getter
    @ToString
    private static class Item {
        private String name;
        private int id;
    }

    @NoArgsConstructor
    @Getter
    @ToString
    private static class Building {
        private String name;
        private int id;
    }

    @NoArgsConstructor
    @Getter
    @ToString
    private static class Recipe {
        private int id;
        private List<List<Integer>> inputs;
        private List<List<Integer>> output;
        private int building;

        @JsonProperty("knowledge multiplier")
        private double kMult;
        @JsonProperty("strength multiplier")
        private double srMult;
        @JsonProperty("stamina multiplier")
        private double stMult;
        @JsonProperty("magic multiplier")
        private double mMult;
    }

}
