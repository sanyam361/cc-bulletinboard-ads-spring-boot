package com.sap.bulletinboard.ads.models;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embeddable;

@Embeddable
public class EntityMetaData {

    @Column(name = "createdat", updatable = false)
    @Convert(converter = InstantAttributeConverter.class)
    private Instant createdAt;

    @Column(name = "modifiedat")
    @Convert(converter = InstantAttributeConverter.class)
    private Instant updatedAt;

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

}
