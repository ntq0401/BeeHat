package com.beehat.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_detail")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    @NotNull(message = "Không được bỏ trống sản phẩm !")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "color_id")
    @NotNull(message = "Không được bỏ trống màu sắc !")
    private Color color;

    @ManyToOne
    @JoinColumn(name = "size_id")
    @NotNull(message = "Không được bỏ trống kích cỡ!")
    private Size size;
    @NotNull(message = "Không được bỏ trống giá!")
    private Integer price;
    @NotNull(message = "Không được bỏ trống số lượng!")
    private Integer stock;
    @NotNull(message = "Không được bỏ trống trạng thái!")
    private Byte status;

    @Column(name = "created_date", insertable = false, updatable = false)
    private LocalDateTime createdDate;
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @PreUpdate
    public void preUpdate() {
        updatedDate = LocalDateTime.now();
    }
}
