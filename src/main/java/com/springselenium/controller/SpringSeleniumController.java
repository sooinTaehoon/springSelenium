package com.springselenium.controller;

import com.springselenium.service.SpringSeleniumService;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SpringSeleniumController {
    private final SpringSeleniumService springSeleniumService;

    @Autowired
    public SpringSeleniumController(SpringSeleniumService springSeleniumService) {
        this.springSeleniumService = springSeleniumService;
    }

    @GetMapping("/review/getProduct")
    public String seleniumStart(@RequestParam(value = "productid", required = true) String productid) throws JSONException {
        return springSeleniumService.mainFunction(productid);
    }
}
