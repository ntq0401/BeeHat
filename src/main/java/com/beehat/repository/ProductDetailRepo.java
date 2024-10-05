package com.beehat.repository;

import com.beehat.entity.ProductDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductDetailRepo extends JpaRepository<ProductDetail, Integer> {
    List<ProductDetail> findByProductId(Integer productId);
}
