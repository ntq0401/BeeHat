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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    @Autowired
    private ProductImageRepo productImageRepo;

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
        model.addAttribute("p", new Product());
        model.addAttribute("isAdd", true);
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
    public String addProduct(@Valid @ModelAttribute("p") Product p, BindingResult rs,
                             Model model, @RequestParam(value = "file") MultipartFile[] files) {
        if (rs.hasErrors()) {
            if (files[0].getOriginalFilename().equals("")) {
                model.addAttribute("errorMessage", "Phải tải lên ít nhất một ảnh!");
                model.addAttribute("isAdd", true);
                return "admin/add-product";
            }
            model.addAttribute("isAdd", true);
            return "admin/add-product";
        }
        // Kiểm tra kỹ hơn đối với files
        if (files[0].getOriginalFilename().equals("")) {
            model.addAttribute("errorMessage", "Phải tải lên ít nhất một ảnh!");
            model.addAttribute("isAdd", true);
            return "admin/add-product";
        }
        String debug = files[0].getOriginalFilename();
        System.out.println(debug);
        // Kiểm tra nếu có file ảnh
        if (files != null && files.length > 0) {
            List<ProductImage> productImages = new ArrayList<>();
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    // Lưu file ảnh vào thư mục trên server
                    String fileName = file.getOriginalFilename();
                    try {
                        // Định nghĩa đường dẫn lưu ảnh
                        String filePath = "src/main/resources/static/product-img/" + fileName;
                        Path path = Paths.get(filePath);
                        Files.write(path, file.getBytes());

                        // Tạo đối tượng ProductImage
                        ProductImage img = new ProductImage();
                        img.setImageUrl("/product-img/" + fileName);  // Đường dẫn truy cập ảnh
                        img.setProduct(p);  // Liên kết ảnh với sản phẩm
                        productImages.add(img);
                    } catch (IOException e) {
                        e.printStackTrace();
                        // Xử lý lỗi khi lưu ảnh
                        model.addAttribute("error", "Error uploading image: " + fileName);
                        return "admin/add-product";
                    }
                }
            }
            // Gán danh sách ảnh vào sản phẩm
            p.setImages(productImages);
        }

        // Lưu sản phẩm vào database (giả định bạn có một service để lưu sản phẩm)
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
    public String updateProduct(@PathVariable("id") int id, @Valid @ModelAttribute("p") Product p, BindingResult rs,
                                @RequestParam("file") MultipartFile[] files, Model model) {
        if (rs.hasErrors()) {
            model.addAttribute("isAdd", false);
            return "admin/add-product";
        }
        p.setId(id);
        p.setSku(productRepo.findById(id).get().getSku());

        // Kiểm tra nếu có file ảnh
        if (!files[0].getOriginalFilename().equals("")) {
            List<ProductImage> productImages = new ArrayList<>();
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    // Lưu file ảnh vào thư mục trên server
                    String fileName = file.getOriginalFilename();
                    try {
                        // Định nghĩa đường dẫn lưu ảnh
                        String filePath = "src/main/resources/static/product-img/" + fileName;
                        Path path = Paths.get(filePath);
                        Files.write(path, file.getBytes());

                        // Tạo đối tượng ProductImage
                        ProductImage img = new ProductImage();
                        img.setImageUrl("/product-img/" + fileName);  // Đường dẫn truy cập ảnh
                        img.setProduct(p);  // Liên kết ảnh với sản phẩm
                        productImages.add(img);
                    } catch (IOException e) {
                        e.printStackTrace();
                        // Xử lý lỗi khi lưu ảnh
                        model.addAttribute("error", "Error uploading image: " + fileName);
                        return "admin/add-product";
                    }
                }
            }
            // Gán danh sách ảnh vào sản phẩm
            productImageRepo.deleteByProductId(id);
            p.setImages(productImages);
        } else {
            List<ProductImage> productImages = productImageRepo.findByProductId(id);
            p.setImages(productImages);
        }
        productRepo.save(p);
        return "redirect:/admin/product/index";
    }
}
