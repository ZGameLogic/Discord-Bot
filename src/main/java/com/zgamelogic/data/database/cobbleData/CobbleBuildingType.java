package com.zgamelogic.data.database.cobbleData;

import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Getter
public enum CobbleBuildingType {
    TOWN_HALL("Town Hall", "Mayor", "th"),
    BUILDER("Builder's Hut", "Builder", "bd"),
    WHEAT_FARM("Wheat Farm", "Wheat Farmer", "wf"),
    FISHERY("Fishery", "Fisherman", "fs"),
    FORESTRY_HUT("Forestry Hut", "Forester", "fh"),
    MINE("Mine", "Miner", "mn");

    private final String friendlyName;
    private final String workerTitle;
    private final String code;

    CobbleBuildingType(String friendlyName, String workerTitle, String code) {
        this.friendlyName = friendlyName;
        this.workerTitle = workerTitle;
        this.code = code;
    }

    public static boolean validName(String name) {
        return Arrays.stream(CobbleBuildingType.values()).anyMatch(cobbleBuildingType -> cobbleBuildingType.friendlyName.equals(name));
    }

    public static CobbleBuildingType fromCode(String code){
        return Arrays.stream(CobbleBuildingType.values()).filter(t -> t.code.equals(code)).findFirst().orElse(null);
    }

    public static CobbleBuildingType fromName(String name) throws CobbleServiceException {
        return Arrays.stream(CobbleBuildingType.values())
            .filter(type -> type.getFriendlyName().equals(name))
            .findFirst()
            .orElseThrow(() -> new CobbleServiceException("Friendly name does not exist"));
    }

    public static String mapBuildings(Map<CobbleBuildingType, Integer> map){
        StringBuilder sb = new StringBuilder();
        map.forEach((key, value) -> {
            if (value == 0) return;
            sb.append(key.getCode() + value);
        });
        return sb.toString();
    }

    public static Map<CobbleBuildingType, Integer> mapBuildings(String resources, boolean includeZero){
        Map<CobbleBuildingType, Integer> map = new HashMap<>();
        if(includeZero){
            map = Arrays.stream(CobbleBuildingType.values())
                .collect(Collectors.toMap(type -> type, type -> 0));
        }
        if(resources == null) return map;

        Pattern pattern = Pattern.compile("([a-zA-Z]+)(\\d+)");
        Matcher matcher = pattern.matcher(resources);

        while (matcher.find()) {
            String code = matcher.group(1);
            int number = Integer.parseInt(matcher.group(2));
            CobbleBuildingType type = CobbleBuildingType.fromCode(code);
            if(type == null) continue;
            map.put(type, number);
        }

        return map;
    }
}
