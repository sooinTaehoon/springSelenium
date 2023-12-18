package com.springselenium.controller;

import com.springselenium.service.SpringSeleniumService;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SpringSeleniumController {
    private final SpringSeleniumService springSeleniumService;

    @Autowired
    public SpringSeleniumController(SpringSeleniumService springSeleniumService) {
        this.springSeleniumService = springSeleniumService;
    }

    @GetMapping("/selenium")
    public String seleniumStart() throws JSONException {
        String url = "8198835007";
        return springSeleniumService.mainFunction(url);
    }
}
