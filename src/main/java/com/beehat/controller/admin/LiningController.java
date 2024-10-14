package com.beehat.controller.admin;

import com.beehat.entity.Lining;
import com.beehat.repository.LiningRepo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/lining")
public class LiningController {
    @Autowired
    LiningRepo liningRepo;

    @ModelAttribute("listLining")
    List<Lining> linings() {
        return liningRepo.findAll();
    }

    @GetMapping("/index")
    public String index(Model model) {
        model.addAttribute("l", new Lining());
        return "admin/lining";
    }

    @PostMapping("/add")
    public String add(@Valid @ModelAttribute("l") Lining lining, BindingResult rs) {
        if (rs.hasErrors()) {
            return "admin/lining";
        }
        liningRepo.save(lining);
        return "redirect:/admin/lining/index";
    }

    @GetMapping("/update/{id}")
    public String update(@PathVariable("id") int id, Model model) {
        model.addAttribute("l", liningRepo.findById(id).orElse(null));
        return "admin/form-lining";
    }

    @PostMapping("/update/{id}")
    public String update(@Valid @ModelAttribute("l") Lining lining, BindingResult rs) {
        if (rs.hasErrors()) {
            return "admin/form-lining";
        }
        liningRepo.save(lining);
        return "redirect:/admin/lining/index";

    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") int id) {
        liningRepo.deleteById(id);
        return "redirect:/admin/lining/index";
    }
}
