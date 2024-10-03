package com.beehat.controller.admin;

import com.beehat.entity.Size;
import com.beehat.repository.SizeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/size")
public class SizeController {
    @Autowired
    SizeRepo sizeRepo;

    @GetMapping("/index")
    public String index(Model model) {
        model.addAttribute("size", sizeRepo.findAll());
        model.addAttribute("m",new Size());
        return "admin/size";
    }
    @PostMapping("/add")
    public String add(@ModelAttribute("m") Size size) {
        sizeRepo.save(size);
        return "redirect:/admin/size/index";
    }
    @GetMapping("/update/{id}")
    public String update(@PathVariable("id") int id, Model model) {
        model.addAttribute("m", sizeRepo.findById(id).orElse(null));
        return "admin/form-size";
    }
    @PostMapping("/update/{id}")
    public String updateS(@ModelAttribute Size size){
        sizeRepo.save(size);
        return "redirect:/admin/size/index";
    }
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") int id) {
        sizeRepo.deleteById(id);
        return "redirect:/admin/size/index";
    }
}
