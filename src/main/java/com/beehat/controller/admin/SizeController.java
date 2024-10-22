package com.beehat.controller.admin;

import com.beehat.entity.Size;
import com.beehat.repository.SizeRepo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/size")
public class SizeController {
    @Autowired
    SizeRepo sizeRepo;

    @ModelAttribute("listSize")
    List<Size> listSize() {
        return sizeRepo.findAll();
    }

    @GetMapping("/index")
    public String index(Model model) {
        model.addAttribute("s", new Size());
        return "admin/size";
    }

    @PostMapping("/index")
    public String add(@Valid @ModelAttribute("s") Size size, BindingResult rs) {
        if (rs.hasErrors()) {
            return "admin/size";
        }
        sizeRepo.save(size);
        return "redirect:/admin/size/index";
    }

    @GetMapping("/update/{id}")
    public String update(@PathVariable("id") int id, Model model) {
        model.addAttribute("s", sizeRepo.findById(id).orElse(null));
        return "admin/form-size";
    }

    @PostMapping("/update/{id}")
    public String updateS(@Valid @ModelAttribute("s") Size size, BindingResult rs) {
        if (rs.hasErrors()) {
            return "admin/form-size";
        }
        sizeRepo.save(size);
        return "redirect:/admin/size/index";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") int id) {
        sizeRepo.deleteById(id);
        return "redirect:/admin/size/index";
    }
}
