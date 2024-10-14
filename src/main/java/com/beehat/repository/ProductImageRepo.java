package com.beehat.repository;

import com.beehat.entity.ProductDetail;
import com.beehat.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepo extends JpaRepository<ProductImage, Integer> {
    List<ProductDetail> findByProductId(Integer productId);
}
