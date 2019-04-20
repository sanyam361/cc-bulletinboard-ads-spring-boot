package com.sap.bulletinboard.ads.models;

import java.time.Instant;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import com.sap.bulletinboard.ads.util.TimeServiceProvider;

public class MetaEntityListener {

    @PrePersist
    public void onCreate(MetaEntity metaEntity) {
        EntityMetaData metaData = getEntityMetaData(metaEntity);
        metaData.setCreatedAt(now());
    }

    @PreUpdate
    public void onUpdate(MetaEntity metaEntity) {
        EntityMetaData metaData = getEntityMetaData(metaEntity);
        metaData.setUpdatedAt(now());
    }

    private EntityMetaData getEntityMetaData(MetaEntity metaEntity) {
        EntityMetaData metaData = metaEntity.getMetaData();
        if (metaData == null) {
            metaData = new EntityMetaData();
            metaEntity.setMetaData(metaData);
        }
        return metaData;
    }

    private Instant now() {
        return TimeServiceProvider.now();
    }

}
