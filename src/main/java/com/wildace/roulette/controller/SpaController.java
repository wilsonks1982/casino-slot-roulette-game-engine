package com.wildace.roulette.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class SpaController {

    private static final Logger log = LoggerFactory.getLogger(SpaController.class);

    @GetMapping(value = {"/", "/dealer", "/admin", "/super", "/login"})
    public String forward() {
        log.info("Forwarding to index.html for SPA routing");
        return "forward:/index.html";
    }
}