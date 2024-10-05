package com.beehat.entity;

import jakarta.persistence.*;
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

    @ManyToOne @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne @JoinColumn(name = "color_id")
    private Color color;

    @ManyToOne @JoinColumn(name = "size_id")
    private Size size;
    private Integer price;
    private Integer stock;

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
