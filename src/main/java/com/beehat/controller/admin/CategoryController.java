package com.beehat.controller.admin;

import com.beehat.entity.Belt;
import com.beehat.entity.Category;
import com.beehat.repository.BeltRepo;
import com.beehat.repository.CategoryRepo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/category")
public class CategoryController {
    @Autowired
    BeltRepo beltRepo;
    @Autowired
    private CategoryRepo categoryRepo;

    @ModelAttribute("iconTitle")
    String iconTitle() {
        return "ph ph-list-dashes fs-3";
    }
    @ModelAttribute("pageTitle")
    String pageTitle() {
        return "Danh mục";
    }
    @ModelAttribute("listCategory")
    List<Category> list(@RequestParam(required = false) String name) {
        if (name == null || name.isBlank()) {
            // Nếu không có tham số tìm kiếm, trả về toàn bộ danh sách
            return categoryRepo.findAll();
        }
        // Tìm kiếm theo tên
        return categoryRepo.findByNameContainingIgnoreCase(name);
    }

    @GetMapping("/index")
    public String belt(Model model, @ModelAttribute("c") Category category) {
        model.addAttribute("category", categoryRepo.findAll());
        return "admin/category/category";
    }

    @PostMapping("/index")
    public String create(@Valid @ModelAttribute("c") Category category, BindingResult rs, RedirectAttributes redirectAttributes) {
        if (rs.hasErrors()) {
            return "admin/category/category";
        }
        categoryRepo.save(category);
        redirectAttributes.addFlashAttribute("message", "Thêm mới thành công!");
        return "redirect:/admin/category/index";
    }


    @GetMapping("/update/{id}")
    public String update(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("c", categoryRepo.findById(id).orElse(null));
        return "admin/category/form-category";
    }

    @PostMapping("/update/{id}")
    public String update(@Valid @ModelAttribute("c") Category category, BindingResult rs) {
        if (rs.hasErrors()) {
            return "admin/belt/form-belt";
        }
        categoryRepo.save(category);
        return "redirect:/admin/category/index";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Integer id) {
        Category category = categoryRepo.findById(id).orElse(null);
        category.setStatus((byte) 0);
        categoryRepo.save(category);
        return "redirect:/admin/category/index";
    }
    @GetMapping("/on/{id}")
    public String turnOn(@PathVariable("id") Integer id) {
        Category category = categoryRepo.findById(id).orElse(null);
        category.setStatus((byte) 1);
        categoryRepo.save(category);
        return "redirect:/admin/category/index";
    }
}
