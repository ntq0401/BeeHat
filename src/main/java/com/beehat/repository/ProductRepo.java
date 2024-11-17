package com.beehat.repository;

import com.beehat.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ProductRepo extends JpaRepository<Product, Integer> {
    @Query("select p from Product p where p.name like ?1")
    public List<Product> findByName(String keyword);
    Page<Product> findAll(Pageable pageable);
    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.promotion = null WHERE p.promotion.id = :promotionId")
    void clearPromotionByPromotionId(@Param("promotionId") Integer promotionId);
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, int id);
    Product findBySku(String sku);
}
