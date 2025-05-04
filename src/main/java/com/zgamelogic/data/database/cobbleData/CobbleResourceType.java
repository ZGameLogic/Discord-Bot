package com.zgamelogic.data.database.cobbleData;

import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Getter
public enum CobbleResourceType {
    PRODUCTION("pd", "Production"),
    WOOD("wd", "Wood"),
    STONE("st", "Stone"),
    METAL("ml", "Metal"),
    MAGIC("mc", "Magic"),
    RATIONS("ra", "Rations");

    public final String code;
    public final String friendlyName;

    CobbleResourceType(String code, String friendlyName) {
        this.code = code;
        this.friendlyName = friendlyName;
    }

    public String getEmojiName(){
        return friendlyName.toLowerCase();
    }

    public static CobbleResourceType fromCode(String code){
        return Arrays.stream(CobbleResourceType.values()).filter(t -> t.code.equals(code)).findFirst().orElse(null);
    }

    public static Map<CobbleResourceType, Integer> mapResources(String resources, boolean includeZero){
        Map<CobbleResourceType, Integer> map = new HashMap<>();
        if(includeZero){
            map = Arrays.stream(CobbleResourceType.values())
                .collect(Collectors.toMap(type -> type, type -> 0));
        }
        if(resources == null) return map;

        Pattern pattern = Pattern.compile("([a-zA-Z]+)(\\d+)");
        Matcher matcher = pattern.matcher(resources);

        while (matcher.find()) {
            String code = matcher.group(1);
            int number = Integer.parseInt(matcher.group(2));
            CobbleResourceType type = CobbleResourceType.fromCode(code);
            if(type == null) continue;
            map.put(type, number);
        }

        return map;
    }
}
