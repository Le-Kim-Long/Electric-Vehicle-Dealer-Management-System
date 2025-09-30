package org.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.security.Principal;

@Controller
public class MainPageControllerAPI {

    @GetMapping("/main")
    public String mainPage(Model model, Principal principal) {
        // Add the logged-in user’s name
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        return "forward:/index.html"; // loads main.html from templates/
    }
}
