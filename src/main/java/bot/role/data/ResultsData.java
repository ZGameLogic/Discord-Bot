package bot.role.data;

import bot.role.data.results.*;
import data.serializing.DataRepository;
import data.serializing.SavableData;
import lombok.Getter;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

@Getter
public class ResultsData {
    private static final String DIR = "arena\\results";

    private DataRepository<ActivityResults> activities;
    private DataRepository<ChallengeFightResults> challenges;
    private DataRepository<EncounterFightResults> encounters;
    private DataRepository<GuildFightResults> guildFights;
    private DataRepository<GuildRaidResults> guildRaids;
    private DataRepository<ItemCraftingResults> itemCrafting;
    private DataRepository<ItemPurchaseResults> itemPurchases;
    private DataRepository<TournamentFightResults> tournamentFights;
    private DataRepository<TournamentResults> tournaments;
    private DataRepository<MiscResults> miscResults;

    public ResultsData(){
        activities = new DataRepository<>(DIR + "\\activities");
        challenges = new DataRepository<>(DIR + "\\challenges");
        encounters = new DataRepository<>(DIR + "\\encounters");
        guildFights = new DataRepository<>(DIR + "\\guild\\fights");
        guildRaids = new DataRepository<>(DIR + "\\guild\\raids");
        itemCrafting = new DataRepository<>(DIR + "\\item\\crafting");
        itemPurchases = new DataRepository<>(DIR + "\\item\\purchases");
        tournamentFights  = new DataRepository<>(DIR + "\\tournament\\fights");
        tournaments = new DataRepository<>(DIR + "\\tournament\\tournament");
        miscResults = new DataRepository<>(DIR + "\\misc");
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
