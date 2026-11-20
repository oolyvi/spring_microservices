package com.atm.webclient;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String homePage() {
        /*
        self port: http://localhost:8089
        api-gateway port (test endpoints here): http://localhost:8765/web/
         */
        return "index";
    }
}