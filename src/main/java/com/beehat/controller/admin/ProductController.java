package com.beehat.controller.admin;

import com.beehat.entity.*;
import com.beehat.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/product")
public class ProductController {
    @Autowired
    ProductRepo productRepo;
    @Autowired
    CategoryRepo categoryRepo;
    @Autowired
    BeltRepo beltRepo;
    @Autowired
    LiningRepo liningRepo;
    @Autowired
    StyleRepo styleRepo;
    @Autowired
    MaterialRepo materialRepo;
    @Autowired
    ProductDetailRepo productDetailRepo;
    @Autowired
    ColorRepo colorRepo;
    @Autowired
    SizeRepo sizeRepo;
    @ModelAttribute("listCategory")
    List<Category> listCategory(){ return categoryRepo.findAll(); }
    @ModelAttribute("listBelt")
    List<Belt> listBelt(){ return beltRepo.findAll(); }
    @ModelAttribute("listLining")
    List<Lining> listLining(){ return liningRepo.findAll(); }
    @ModelAttribute("listStyle")
    List<Style> listStyle() { return styleRepo.findAll(); }
    @ModelAttribute("listMaterial")
    List<Material> listMaterial() { return materialRepo.findAll(); }
    @ModelAttribute("listColor")
    List<Color> listColor() {return colorRepo.findAll(); }
    @ModelAttribute("listSize")
    List<Size> listSize() { return sizeRepo.findAll(); }
    @GetMapping("/index")
    public String product(Model model) {
        model.addAttribute("product", productRepo.findAll());
        return "admin/product";
    }
    @GetMapping("/search")
    public String search(@RequestParam(defaultValue = " ") String keyword,Model model) {
        model.addAttribute("product",productRepo.findByName("%"+keyword+"%"));
        return "admin/product";
    }
    @GetMapping("/add-product")
    public String addProduct(Model model) {
        model.addAttribute("p", new Product());
        model.addAttribute("isAdd", true);
        return "admin/add-product";
    }
    @PostMapping("/add-product")
    public String addProduct(@ModelAttribute("p") Product p) {
        productRepo.save(p);
        return "redirect:/admin/product/index";
    }
    @GetMapping("/add-product-detail/{id}")
    public String addProductDetail(@PathVariable("id") int id, Model model) {
        //tạo ra đối tượng mới để binding dữ liệu với form
        ProductDetail productDetail = new ProductDetail();
        //fix cứng id sản phẩm cho form
        productDetail.setProduct(productRepo.findById(id).orElse(null));
        model.addAttribute("pd", productDetail);
        //hiển thị danh sách sản phẩm chi tiết của sản phẩm
        model.addAttribute("prd", productDetailRepo.findByProductId(id));
        model.addAttribute("colors", new ArrayList<>()); // Danh sách màu sẽ được chọn
        return "admin/add-product-detail";
    }
    @PostMapping("/add-product-detail/{id}")
    public String addProductDetail(@PathVariable("id") int id,@ModelAttribute("pd") ProductDetail pd) {

        pd.setId(null);
        pd.setProduct(productRepo.findById(id).get());
        productDetailRepo.save(pd);
        return "redirect:/admin/product/add-product-detail/" + id;
    }
    @GetMapping("/update-product/{id}")
    public String updateProduct(@PathVariable("id") int id, Model model) {
        model.addAttribute("p", productRepo.findById(id).orElse(null));
        model.addAttribute("isAdd", false);
        return "admin/add-product";
    }
    @PostMapping("/update-product/{id}")
    public String updateProduct(@PathVariable("id") int id,@ModelAttribute("p") Product p) {
        p.setId(id);
        productRepo.save(p);
        return "redirect:/admin/product/index";
    }
}
