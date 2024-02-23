package edu.lucankri.gamesnake.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GameController {
    @GetMapping("/")
    public String startIndex() {
        return "index";
    }

    @GetMapping("/version")
    public String getVersion() {
        return getClass().getPackage().getImplementationVersion();
    }
}
