package com.sap.bulletinboard.ads.models;

import java.sql.Timestamp;
import java.time.Instant;

import javax.persistence.AttributeConverter;

public class InstantAttributeConverter implements AttributeConverter<Instant, Timestamp> {

    @Override
    public Timestamp convertToDatabaseColumn(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }

    @Override
    public Instant convertToEntityAttribute(Timestamp sqlTimestamp) {
        return sqlTimestamp == null ? null : sqlTimestamp.toInstant();
    }
}
