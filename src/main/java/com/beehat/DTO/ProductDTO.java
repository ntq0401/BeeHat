package com.beehat.DTO;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class ProductDTO {
    private Integer id;
    private String name;
    private String description;
    private Integer category;
    private Integer material;
    private Integer lining;
    private Integer belt;
    private Integer style;
    private Byte status;

    private List<MultipartFile> images;

    // Getters và Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    public Integer getMaterial() {
        return material;
    }

    public void setMaterial(Integer material) {
        this.material = material;
    }

    public Integer getLining() {
        return lining;
    }

    public void setLining(Integer lining) {
        this.lining = lining;
    }

    public Integer getBelt() {
        return belt;
    }

    public void setBelt(Integer belt) {
        this.belt = belt;
    }

    public Integer getStyle() {
        return style;
    }

    public void setStyle(Integer style) {
        this.style = style;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public List<MultipartFile> getImages() {
        return images;
    }

    public void setImages(List<MultipartFile> images) {
        this.images = images;
    }
}
