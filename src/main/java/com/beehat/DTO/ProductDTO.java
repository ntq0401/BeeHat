package com.beehat.DTO;

import com.beehat.entity.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProductDTO {

    private Integer id;

    @NotBlank(message = "Tên sản phẩm không được bỏ trống !")
    private String name;

    @NotBlank(message = "Không được bỏ trống mô tả !")
    private String description;

    @NotNull(message = "Không được bỏ trống ảnh !")
    private List<ProductImage> images = new ArrayList<>();

    @NotNull(message = "Không được bỏ trống danh mục !")
    private Integer categoryId;

    @NotNull(message = "Không được bỏ trống chất liệu !")
    private Integer materialId;

    @NotNull(message = "Không được bỏ trống vải lót !")
    private Integer liningId;

    @NotNull(message = "Không được bỏ trống đai mũ!")
    private Integer beltId;

    @NotNull(message = "Không được bỏ trống kiểu dáng !")
    private Integer styleId;

    @NotNull(message = "Không được bỏ trống trạng thái !")
    @Min(value = 0, message = "Status must be at least 0")
    @Max(value = 1, message = "Status can only be 0 or 1")
    private Byte status;

    public Product toEntity() {
        Product product = new Product();

        product.setId(id);
        product.setName(name);
        product.setDescription(description);

        // Set danh mục sản phẩm
        if (categoryId != null) {
            Category category = new Category();
            category.setId(categoryId);
            product.setCategory(category);
        }

        // Set chất liệu sản phẩm
        if (materialId != null) {
            Material material = new Material();
            material.setId(materialId);
            product.setMaterial(material);
        }

        // Set vải lót
        if (liningId != null) {
            Lining lining = new Lining();
            lining.setId(liningId);
            product.setLining(lining);
        }

        // Set đai mũ
        if (beltId != null) {
            Belt belt = new Belt();
            belt.setId(beltId);
            product.setBelt(belt);
        }

        // Set kiểu dáng
        if (styleId != null) {
            Style style = new Style();
            style.setId(styleId);
            product.setStyle(style);
        }

        // Set trạng thái
        product.setStatus(status);

        // Thêm danh sách ảnh
        if (images != null && !images.isEmpty()) {
            List<ProductImage> productImages = new ArrayList<>();
            for (ProductImage image : images) {
                ProductImage imgEntity = new ProductImage();
                imgEntity.setImageUrl(image.getImageUrl());  // Assuming ProductImage has 'url' field
                imgEntity.setProduct(product); // Set sản phẩm cho từng ảnh
                productImages.add(imgEntity);
            }
            product.setImages(productImages); // Gán danh sách ảnh cho sản phẩm
        }

        return product;

    }
}
