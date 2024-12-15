package com.beehat.DTO;

import com.beehat.entity.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class ProductDTO {
    private Integer id;
    private String name;
    private String sku;
    private String description;
    private List<ProductImage> images;
    private List<ProductDetail> productDetail;
    private Style style;
    private Category category;
    private Material material;
    private Lining lining;
    private Belt belt;
    private int lowestPrice;
    private int highestPrice;
    private int totalStock;
    private List<Color> colors;
    private List<Size> sizes;
    private Promotion promotion;
    private Byte status;
    private LocalDateTime createdDate;

    // Constructor
    public ProductDTO(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.sku = product.getSku();
        this.description = product.getDescription();
        this.images = product.getImages();
        this.belt = product.getBelt();
        this.lining = product.getLining();
        this.material = product.getMaterial();
        this.style = product.getStyle();
        this.category = product.getCategory();
        this.productDetail = product.getProductDetail().stream()
                .filter(detail -> detail.getStatus() == 1)
                .collect(Collectors.toList());
        this.lowestPrice = calculateLowestPrice(this.productDetail);
        this.highestPrice = calculateHighestPrice(this.productDetail);
        this.totalStock = getTotalStock();
        this.colors = getColors(this.productDetail);
        this.sizes = getSizes(this.productDetail);
        this.promotion = product.getPromotion();
        this.status = product.getStatus();
        this.createdDate = product.getCreatedDate();
    }

    public int getTotalStock() {
        return productDetail.stream()
                .filter(detail -> detail.getStock() != null)
                .mapToInt(ProductDetail::getStock)
                .sum();
    }

    // Method to calculate lowest price
    private int calculateLowestPrice(List<ProductDetail> productDetails) {
        return productDetails.stream()
                .mapToInt(ProductDetail::getPrice)
                .min()
                .orElse(0);  // Default to 0 if no prices found
    }

    // Method to calculate
    private int calculateHighestPrice(List<ProductDetail> productDetails) {
        return productDetails.stream()
                .mapToInt(ProductDetail::getPrice)
                .max()
                .orElse(0);  // Default to 0 if no prices found
    }

    private List<Color> getColors(List<ProductDetail> productDetails) {
        return productDetails.stream()
                .map(ProductDetail::getColor)
                .filter(java.util.Objects::nonNull) // Bỏ qua giá trị null
                .distinct() // Lọc trùng lặp
                .collect(Collectors.toList());
    }

    private List<Size> getSizes(List<ProductDetail> productDetails) {
        return productDetails.stream()
                .map(ProductDetail::getSize)
                .filter(java.util.Objects::nonNull) // Bỏ qua giá trị null
                .distinct() // Lọc trùng lặp
                .collect(Collectors.toList());
    }
}
