package com.zgamelogic.data.database.cobbleData;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum CobbleBuildingType {
    TOWN_HALL("Town Hall", "Mayor"),
    BUILDER("Builder's Hut", "Builder"),
    WHEAT_FARM("Wheat Farm", "Wheat Farmer"),
    FISHERY("Fishery", "Fisherman"),
    FORESTRY_HUT("Forestry Hut", "Forester"),
    MINE("Mine", "Miner");

    private final String friendlyName;
    private final String workerTitle;

    CobbleBuildingType(String friendlyName, String workerTitle) {
        this.friendlyName = friendlyName;
        this.workerTitle = workerTitle;
    }

    public static boolean validName(String name) {
        return Arrays.stream(CobbleBuildingType.values()).anyMatch(cobbleBuildingType -> cobbleBuildingType.friendlyName.equals(name));
    }

    public static CobbleBuildingType fromName(String name) throws CobbleServiceException {
        return Arrays.stream(CobbleBuildingType.values())
            .filter(type -> type.getFriendlyName().equals(name))
            .findFirst()
            .orElseThrow(() -> new CobbleServiceException("Friendly name does not exist"));
    }
}
