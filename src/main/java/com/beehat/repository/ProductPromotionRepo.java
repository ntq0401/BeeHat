package com.beehat.repository;

import com.beehat.entity.ProductPromotion;
import com.beehat.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ProductPromotionRepo extends JpaRepository<ProductPromotion,Integer> {
    List<ProductPromotion> findAllByPromotionAndStatus(Promotion promotion, byte status);
    List<ProductPromotion> findByPromotionIdAndStatus(Integer promotionId,Byte status);
}
