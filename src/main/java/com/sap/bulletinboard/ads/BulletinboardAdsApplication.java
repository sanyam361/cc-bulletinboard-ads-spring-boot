package com.sap.bulletinboard.ads;

import org.springframework.boot.Banner.Mode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

@SpringBootApplication
public class BulletinboardAdsApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(BulletinboardAdsApplication.class);
        app.setBannerMode(Mode.OFF);
        app.run(args);
        new DummyCommand().execute();
    }

    // Workaround for https://github.com/Netflix/Hystrix/issues/1117
    private static class DummyCommand extends HystrixCommand<Void> {
        DummyCommand() {
            super(HystrixCommandGroupKey.Factory.asKey("Dummy"));
        }

        @Override
        protected Void run() {
            return null;
        }
    }

}
