package com.beehat.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
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
    private Color color;

    @ManyToOne
    @JoinColumn(name = "size_id")
    private Size size;
    @NotNull(message = "Không được bỏ trống giá!")
    @Min(value = 0,message = "Giá sản phẩm phải lớn hơn 0")
    @Max(value = 100000000, message = "Giá sản phẩm không được vượt quá 100 triệu!")
    private Integer price;
    @NotNull(message = "Không được bỏ trống số lượng!")
    @Max(value = 100000000, message = "Số lượng sản phẩm quá lớn!")
    @Min(value = 0,message = "Số lượng phải lớn hơn 0")
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
