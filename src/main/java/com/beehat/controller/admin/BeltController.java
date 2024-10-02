package com.beehat.controller.admin;

import com.beehat.entity.Belt;
import com.beehat.repository.BeltRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/belt")
public class BeltController {
    @Autowired
    BeltRepo beltRepo;
    @GetMapping("/index")
    public String belt(Model model, @ModelAttribute("b") Belt belt) {
        model.addAttribute("belt", beltRepo.findAll());
        return "admin/belt";
    }
    @PostMapping("/add")
    public String create(@ModelAttribute Belt belt) {
        beltRepo.save(belt);
        return "redirect:/admin/belt/index";
    }
    @GetMapping("/update/{id}")
    public String update(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("belt", beltRepo.findById(id).orElse(null));
        return "admin/form-belt";
    }
    @PostMapping("/update/{id}")
    public String update(@ModelAttribute Belt belt, @PathVariable("id") Integer id) {
        beltRepo.save(belt);
        return "redirect:/admin/belt/index";
    }
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Integer id) {
        beltRepo.deleteById(id);
        return "redirect:/admin/belt/index";
    }
}
