package bot.role.data;

import bot.role.data.item.ShopItem;
import bot.role.data.jsonConfig.GameConfigValues;
import bot.role.data.structures.*;
import data.serializing.DataRepository;
import data.serializing.SavableData;
import lombok.Getter;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

@Getter
public class Data {
    private static final String DIR = "arena";

    private DataRepository<Player> players;
    private DataRepository<Encounter> encounters;
    private DataRepository<Activity> activities;
    private DataRepository<ShopItem> shopItems;
    private DataRepository<Guild> guilds;
    private DataRepository<KingData> kingData;
    private DataRepository<GameConfigValues> gameConfig;
    private DataRepository<General> general;

    public Data(){
        players = new DataRepository<>(DIR + "\\players");
        encounters = new DataRepository<>(DIR + "\\encounters");
        activities = new DataRepository<>(DIR +"\\activities");
        guilds = new DataRepository<>(DIR + "\\guilds");
        kingData = new DataRepository<>(DIR + "\\king");
        shopItems = new DataRepository<>(DIR + "\\shop items");
        gameConfig = new DataRepository<>(DIR + "\\game config data");
        general = new DataRepository<>(DIR + "\\general");

        if(general.getFiles().length == 0){
            General g = new General();
            general.saveSerialized(g);
        }

        if(gameConfig.getFiles().length == 0){
            GameConfigValues gcv = new GameConfigValues();
            gameConfig.saveSerialized(gcv);
        }
    }

    /**
     * Saves a type of data
     * @param data
     */
    public void saveData(SavableData...data){
        for(Field f : Data.class.getDeclaredFields()){
            if(f.getType() != String.class) {
                ParameterizedType dataListType = (ParameterizedType) f.getGenericType();
                Class<?> fieldType = (Class<?>) dataListType.getActualTypeArguments()[0];
                if (data[0].getClass() == fieldType) {
                    try {
                        DataRepository dataRepository = (DataRepository) f.get(this);
                        dataRepository.saveSerialized(data);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
}
