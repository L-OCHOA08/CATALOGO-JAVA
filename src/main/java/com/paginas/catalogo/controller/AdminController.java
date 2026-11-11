package com.paginas.catalogo.controller;

import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    @GetMapping("/admin")
    public String panel(Principal principal, Model model) {
        model.addAttribute("usuario", principal.getName());
        return "admin/index";
    }
}