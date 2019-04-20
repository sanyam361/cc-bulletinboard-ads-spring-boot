package com.sap.bulletinboard.ads.util;

import java.time.Instant;

/* Implementations used in production must be thread-safe! */
@FunctionalInterface
public interface TimeService {

    Instant now();

}
