package com.beehat.controller.admin;

import com.beehat.entity.Material;
import com.beehat.repository.MaterialRepo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/material")
public class MaterialController {
    @Autowired
    MaterialRepo materialRepo;

    @ModelAttribute("listMaterial")
    List<Material> listMaterial() {
        return materialRepo.findAll();
    }

    @GetMapping("/index")
    public String index(Model model) {
        model.addAttribute("m", new Material());
        return "admin/material";
    }

    @PostMapping("/index")
    public String add(@Valid @ModelAttribute("m") Material material, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "admin/material";
        }
        materialRepo.save(material);
        return "redirect:/admin/material/index";
    }

    @GetMapping("/update/{id}")
    public String update(@PathVariable int id, Model model) {
        model.addAttribute("m", materialRepo.findById(id).orElse(null));
        return "admin/form-material";
    }

    @PostMapping("/update/{id}")
    public String update(@Valid @ModelAttribute("m") Material material, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "admin/form-material";
        }
        materialRepo.save(material);
        return "redirect:/admin/material/index";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable int id) {
        materialRepo.deleteById(id);
        return "redirect:/admin/material/index";
    }
}
