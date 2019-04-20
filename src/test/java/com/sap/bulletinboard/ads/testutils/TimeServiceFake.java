package com.sap.bulletinboard.ads.testutils;

import java.time.Instant;

import com.sap.bulletinboard.ads.util.TimeService;

public class TimeServiceFake implements TimeService {

    private final Instant instant;

    public TimeServiceFake(Instant instant) {
        this.instant = instant;
    }

    @Override
    public Instant now() {
        return instant;
    }

}
