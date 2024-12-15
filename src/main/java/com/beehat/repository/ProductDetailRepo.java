package com.beehat.repository;

import com.beehat.entity.ProductDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductDetailRepo extends JpaRepository<ProductDetail, Integer> {
    List<ProductDetail> findByProductId(Integer productId);
    Page<ProductDetail> findByStatus(Byte status, Pageable pageable);
    // Lấy danh sách ProductDetail theo id của Promotion
    @Query("SELECT pd FROM ProductDetail pd WHERE pd.product.id IN " +
            "(SELECT pp.product.id FROM ProductPromotion pp WHERE pp.promotion.id = :promotionId AND pp.status = 1)")
    List<ProductDetail> findByPromotionId(@Param("promotionId") Integer promotionId);
    boolean existsByProductIdAndColorIdAndSizeId(Integer productId, Integer colorId, Integer sizeId);
    ProductDetail findTopByProductIdOrderByPriceAsc(int id);

    ProductDetail findTopByProductIdOrderByPriceDesc(int id);
    ProductDetail findByProductIdAndColorIdAndSizeIdAndStatus(Integer productId,Integer colorId, Integer sizeId, Byte status);
    @Query("SELECT pd FROM ProductDetail pd " +
            "JOIN pd.product p " +
            "WHERE pd.status = 1 " + // Trạng thái đang hoạt động
            "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
            "AND (:materialId IS NULL OR p.material.id = :materialId) " +
            "AND (:colorId IS NULL OR pd.color.id = :colorId) " +
            "AND (:sizeId IS NULL OR pd.size.id = :sizeId) " +
            "AND (:styleId IS NULL OR p.style.id = :styleId) " +
            "AND (:liningId IS NULL OR p.lining.id = :liningId) " +
            "AND (:beltId IS NULL OR p.belt.id = :beltId) " +
            "AND (:keyword IS NULL OR LOWER(p.name) LIKE CONCAT('%', LOWER(TRIM(:keyword)), '%'))")
    Page<ProductDetail> findByCriteria(
            Integer categoryId,
            Integer materialId,
            Integer colorId,
            Integer sizeId,
            Integer styleId,
            Integer liningId,
            Integer beltId,
            String keyword,
            Pageable pageable
    );
    @Query("SELECT pd FROM ProductDetail pd " +
            "WHERE (:colorId IS NULL OR pd.color.id = :colorId) " +
            "AND (:sizeId IS NULL OR pd.size.id = :sizeId) " +
            "AND (:minPrice IS NULL OR pd.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR pd.price <= :maxPrice)")
    List<ProductDetail> findByColorSizeAndPrice(@Param("colorId") Integer colorId,
                                                @Param("sizeId") Integer sizeId,
                                                @Param("minPrice") Integer minPrice,
                                                @Param("maxPrice") Integer maxPrice);
    @Query("SELECT COUNT(pd) FROM ProductDetail pd WHERE pd.product = ?1 and pd.status = 1")
    int countProductDetailsByStatus(int productId);
}
