package com.sap.bulletinboard.ads.controllers.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AdvertisementListDto {

    @JsonProperty("value")
    public List<AdvertisementDto> advertisements;

}