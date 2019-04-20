package com.sap.bulletinboard.ads.controllers.dto;

import java.math.BigDecimal;
import java.time.Instant;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class AdvertisementDto {

    public Long id;

    @NotBlank
    public String title;

    @NotNull
    public BigDecimal price;

    @NotBlank
    public String contact;

    @NotBlank
    public String currency;

    public String category;

    public Instant purchasedOn;

    public MetaData metadata = new MetaData();

    public static class MetaData {
        public String createdAt;
        public String modifiedAt;
        public Long version = 0L;
    }
}
