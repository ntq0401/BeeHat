package com.beehat.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @NotBlank(message = "Tên sản phẩm không được bỏ trống !")
    private String name;

    @NotBlank(message = "Không được bỏ trống mô tả !")
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

    @PreUpdate
    public void preUpdate() {
        updatedDate = LocalDateTime.now();
    }
}
