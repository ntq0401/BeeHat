package com.beehat.repository;

import com.beehat.entity.ProductDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductDetailRepo extends JpaRepository<ProductDetail, Integer> {
    List<ProductDetail> findByProductId(Integer productId);
    // Lấy danh sách ProductDetail theo id của Promotion
    @Query("SELECT pd FROM ProductDetail pd WHERE pd.product.id IN " +
            "(SELECT pp.product.id FROM ProductPromotion pp WHERE pp.promotion.id = :promotionId)")
    List<ProductDetail> findByPromotionId(@Param("promotionId") Integer promotionId);
}
