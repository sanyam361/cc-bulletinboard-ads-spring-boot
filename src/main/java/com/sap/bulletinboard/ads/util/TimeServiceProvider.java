package com.sap.bulletinboard.ads.util;

import java.time.Instant;

public class TimeServiceProvider {

    private static volatile TimeService timeService = Instant::now;

    /** Can be used to mock time during tests */
    public static void setTimeService(TimeService newService) {
        timeService = newService;
    }

    public static Instant now() {
        return timeService.now();
    }

}
