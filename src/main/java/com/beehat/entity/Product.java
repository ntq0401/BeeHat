package com.beehat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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
    private String name;
    private String image;
    private Integer price;
    private String description;
    @ManyToOne @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne @JoinColumn(name = "promotion_id")
    private Promotion promotion;

    @ManyToOne @JoinColumn(name = "material_id")
    private Material material;

    @ManyToOne @JoinColumn(name = "lining_id")
    private Lining lining;

    @ManyToOne @JoinColumn(name = "belt_id")
    private Belt belt;

    @ManyToOne @JoinColumn(name = "style_id")
    private Style style;

    private Byte status;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
}
