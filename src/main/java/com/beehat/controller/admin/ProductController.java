package com.beehat.controller.admin;

import com.beehat.DTO.ProductDTO;
import com.beehat.entity.*;
import com.beehat.repository.*;
import com.beehat.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    @Autowired
    ProductService productService;

    @ModelAttribute("listCategory")
    List<Category> listCategory() {
        return categoryRepo.findByStatus(Byte.valueOf("1"));
    }

    @ModelAttribute("listBelt")
    List<Belt> listBelt() {
        return beltRepo.findByStatus(Byte.valueOf("1"));
    }

    @ModelAttribute("listLining")
    List<Lining> listLining() {
        return liningRepo.findByStatus(Byte.valueOf("1"));
    }

    @ModelAttribute("listStyle")
    List<Style> listStyle() {
        return styleRepo.findByStatus(Byte.valueOf("1"));
    }

    @ModelAttribute("listMaterial")
    List<Material> listMaterial() {
        return materialRepo.findByStatus(Byte.valueOf("1"));
    }

    @ModelAttribute("listColor")
    List<Color> listColor() {
        return colorRepo.findByStatus(Byte.valueOf("1"));
    }

    @ModelAttribute("listSize")
    List<Size> listSize() {
        return sizeRepo.findByStatus(Byte.valueOf("1"));
    }

    @GetMapping("/index")
    public String product(Model model) {
        model.addAttribute("product", productRepo.findAll());
        return "admin/product";
    }

    @GetMapping("/search")
    public String search(@RequestParam(defaultValue = " ") String keyword, Model model) {
        model.addAttribute("product", productRepo.findByName("%" + keyword + "%"));
        return "admin/product";
    }

    @GetMapping("/add-product")
    public String addProduct(Model model) {
        model.addAttribute("p", new Product());
        model.addAttribute("isAdd", true);
        return "admin/add-product";
    }

    @PostMapping("/add-product")
    public String addProduct(@Valid @ModelAttribute("p") ProductDTO p, BindingResult rs, Model model) throws IOException {
        if (rs.hasErrors()) {
            model.addAttribute("isAdd", true);
            return "admin/add-product";
        }
        productService.saveProduct(p);
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
        List<ProductDetail> productDetails = productDetailRepo.findByProductId(id);
        Map<Color, List<ProductDetail>> groupedByColor = productDetails.stream()
                .collect(Collectors.groupingBy(ProductDetail::getColor));
        model.addAttribute("groupedByColor", groupedByColor);

        model.addAttribute("colors", new ArrayList<>()); // Danh sách màu sẽ được chọn
        return "admin/add-product-detail";
    }

    @PostMapping("/add-product-detail/{id}")
    public String addProductDetail(@Valid @PathVariable("id") int id,
                                   @ModelAttribute("pd") ProductDetail pd,
                                   @RequestParam("colors") List<Integer> colorIds,
                                   @RequestParam("sizes") List<Integer> sizeIds, BindingResult rs) {
        if (rs.hasErrors() || colorIds.size() == 0 || sizeIds.size() == 0) {
            return "admin/add-product-detail";
        }
        for (Integer colorId : colorIds) {
            for (Integer sizeId : sizeIds) {
                ProductDetail productDetail = new ProductDetail();
                productDetail.setId(null);
                productDetail.setProduct(productRepo.findById(id).get());
                productDetail.setColor(colorRepo.findById(colorId).orElse(null));
                productDetail.setSize(sizeRepo.findById(sizeId).orElse(null));
                productDetail.setPrice(pd.getPrice());
                productDetail.setStock(pd.getStock());
                productDetail.setStatus(pd.getStatus());
                productDetailRepo.save(productDetail);
            }
        }
        return "redirect:/admin/product/add-product-detail/" + id;
    }

    @GetMapping("/update-product/{id}")
    public String updateProduct(@PathVariable("id") int id, Model model) {
        model.addAttribute("p", productRepo.findById(id).orElse(null));
        model.addAttribute("isAdd", false);
        return "admin/add-product";
    }

    @PostMapping("/update-product/{id}")
    public String updateProduct(@PathVariable("id") int id, @ModelAttribute("p") Product p) {
        p.setId(id);
        productRepo.save(p);
        return "redirect:/admin/product/index";
    }
}
