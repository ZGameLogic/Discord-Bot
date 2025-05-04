package com.zgamelogic.data.database.cobbleData;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Map;

@Converter
public class CobbleBuildingConverter implements AttributeConverter<Map<CobbleBuildingType, Integer>, String> {
    @Override
    public String convertToDatabaseColumn(Map<CobbleBuildingType, Integer> attribute) {
        return CobbleBuildingType.mapBuildings(attribute);
    }

    @Override
    public Map<CobbleBuildingType, Integer> convertToEntityAttribute(String dbData) {
        return CobbleBuildingType.mapBuildings(dbData, true);
    }
}
