package bot.role.data;

import bot.role.data.results.*;
import data.serializing.DataCacher;
import data.serializing.SavableData;
import lombok.Getter;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

@Getter
public class ResultsData {
    private static final String DIR = "arena\\results";

    private DataCacher<ActivityResults> activities;
    private DataCacher<ChallengeFightResults> challenges;
    private DataCacher<EncounterFightResults> encounters;
    private DataCacher<GuildFightResults> guildFights;
    private DataCacher<GuildRaidResults> guildRaids;
    private DataCacher<ItemCraftingResults> itemCrafting;
    private DataCacher<ItemPurchaseResults> itemPurchases;
    private DataCacher<TournamentFightResults> tournamentFights;
    private DataCacher<TournamentResults> tournaments;

    public ResultsData(){
        activities = new DataCacher<>(DIR + "\\activities");
        challenges = new DataCacher<>(DIR + "\\challenges");
        encounters = new DataCacher<>(DIR + "\\encounters");
        guildFights = new DataCacher<>(DIR + "\\guild\\fights");
        guildRaids = new DataCacher<>(DIR + "\\guild\\raids");
        itemCrafting = new DataCacher<>(DIR + "\\item\\crafting");
        itemPurchases = new DataCacher<>(DIR + "\\item\\purchases");
        tournamentFights  = new DataCacher<>(DIR + "\\tournament\\fights");
        tournaments = new DataCacher<>(DIR + "\\tournament\\tournament");
    }

    /**
     * Saves a type of data
     * @param data
     */
    public void saveData(SavableData...data){
        for(Field f : ResultsData.class.getDeclaredFields()){
            if(f.getType() != String.class) {
                ParameterizedType dataListType = (ParameterizedType) f.getGenericType();
                Class<?> fieldType = (Class<?>) dataListType.getActualTypeArguments()[0];
                if (data[0].getClass() == fieldType) {
                    try {
                        DataCacher dataCacher = (DataCacher) f.get(this);
                        dataCacher.saveSerialized(data);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

}
