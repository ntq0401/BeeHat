package com.beehat.controller.admin;

import com.beehat.entity.Lining;
import com.beehat.repository.LiningRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/lining")
public class LiningController {
    @Autowired
    LiningRepo liningRepo;
    @GetMapping("/index")
    public String index(Model model) {
        model.addAttribute("lining", liningRepo.findAll());
        model.addAttribute("l",new Lining());
        return "admin/lining";
    }
    @PostMapping("/add")
    public String add(@ModelAttribute("l") Lining lining) {
        liningRepo.save(lining);
        return "redirect:/admin/lining/index";
    }
    @GetMapping("/update/{id}")
    public String update(@PathVariable("id") int id, Model model) {
        model.addAttribute("lining", liningRepo.findById(id).orElse(null));
        return "admin/form-lining";
    }
    @PostMapping("/update/{id}")
    public String update(@ModelAttribute Lining lining, @PathVariable("id") int id) {
        liningRepo.save(lining);
        return "redirect:/admin/lining/index";
    }
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") int id) {
        liningRepo.deleteById(id);
        return "redirect:/admin/lining/index";
    }
}
