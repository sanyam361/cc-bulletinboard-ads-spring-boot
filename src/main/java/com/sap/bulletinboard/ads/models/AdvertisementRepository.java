package com.sap.bulletinboard.ads.models;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {
    List<Advertisement> findByTitle(String title);

    List<Advertisement> findByCategory(String category);

}