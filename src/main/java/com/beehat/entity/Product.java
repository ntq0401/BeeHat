package com.beehat.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Tên sản phẩm không được bỏ trống!")
    @Size(max = 100, message = "Tên không được dài quá 100 ký tự")
    private String name;

    @Column(name = "sku", unique = true)
    private String sku;

    @NotBlank(message = "Không được bỏ trống mô tả !")
    @Size(max = 255, message = "Mô tả không được dài quá 255 ký tự")
    private String description;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    @NotNull(message = "Không được bỏ trống ảnh !")
    private List<ProductImage> images = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "category_id")
    @NotNull(message = "Không được bỏ trống danh mục !")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "promotion_id")
    private Promotion promotion;

    @ManyToOne
    @JoinColumn(name = "material_id")
    @NotNull(message = "Không được bỏ trống chất liệu !")
    private Material material;

    @ManyToOne
    @JoinColumn(name = "lining_id")
    @NotNull(message = "Không được bỏ trống vải lót !")
    private Lining lining;

    @ManyToOne
    @JoinColumn(name = "belt_id")
    @NotNull(message = "Không được bỏ trống đai mũ!")
    private Belt belt;

    @ManyToOne
    @JoinColumn(name = "style_id")
    @NotNull(message = "Không được bỏ trống kiểu dáng !")
    private Style style;

    @NotNull(message = "Không được bỏ trống trạng thái !")
    @Min(value = 0, message = "Status must be at least 0")
    @Max(value = 1, message = "Status can only be 0 or 1")
    private Byte status;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductDetail> productDetail = new ArrayList<>();

    @Column(name = "created_date", insertable = false, updatable = false)
    private LocalDateTime createdDate;
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
    @PrePersist
    public void prePersist() {
        // Nếu SKU chưa có, tự động tạo mới SKU
        if (this.sku == null || this.sku.isEmpty()) {
            this.sku = generateSKU();
        }
    }

    // Phương thức sinh SKU
    private String generateSKU() {
        // Sử dụng 3 ký tự đầu tiên của tên sản phẩm (đã bỏ khoảng trắng)
        String productCode = UUID.randomUUID().toString().substring(0, 5).toUpperCase();

        // Sinh chuỗi ngẫu nhiên 5 ký tự
        String randomUUID = UUID.randomUUID().toString().substring(0, 5).toUpperCase();

        // Tạo SKU bằng cách kết hợp productCode, timeStamp, và randomUUID
        return productCode + "-"  + randomUUID;
    }

    public int getTotalStock() {
        return productDetail.stream()
                .filter(detail -> detail.getStock() != null)
                .mapToInt(ProductDetail::getStock)
                .sum();
    }
    @PreUpdate
    public void preUpdate() {
        updatedDate = LocalDateTime.now();
    }
}
