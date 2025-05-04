package com.zgamelogic.data.database.cobbleData;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Map;

@Converter
public abstract class CobbleResourceConverter implements AttributeConverter<Map<CobbleResourceType, Integer>, String> {
    @Override
    public String convertToDatabaseColumn(Map<CobbleResourceType, Integer> attribute) {
        // TODO conversion
        return "";
    }

    @Override
    public Map<CobbleResourceType, Integer> convertToEntityAttribute(String dbData) {
        // TODO conversion
        return Map.of();
    }
}
