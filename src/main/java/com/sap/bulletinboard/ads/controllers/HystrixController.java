package com.sap.bulletinboard.ads.controllers;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sap.bulletinboard.ads.services.GetUserCommand;

@RestController
public class HystrixController {

    @PutMapping("/hystrix/{timeout}")
    public void setTimeoutforUserservice(@PathVariable("timeout") int timeout) {
        GetUserCommand.setTimeout(timeout);
    }
}
