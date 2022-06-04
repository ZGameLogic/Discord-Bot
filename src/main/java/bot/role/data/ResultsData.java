package bot.role.data;

import bot.role.data.results.*;
import data.serializing.DataCacher;
import lombok.Getter;

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

}
