package com.beehat.service;

import com.beehat.entity.Product;
import com.beehat.entity.ProductPromotion;
import com.beehat.entity.Promotion;
import com.beehat.repository.ProductPromotionRepo;
import com.beehat.repository.ProductRepo;
import com.beehat.repository.PromotionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PromotionService {
//    private static final Logger logger = LoggerFactory.getLogger(PromotionService.class);
    @Autowired
    PromotionRepo promotionRepo;
    @Autowired
    private ProductPromotionRepo productPromotionRepo;
    @Autowired
    private ProductRepo productRepo;
    @Transactional
    @Scheduled(cron = "0 * * * * ?") // Chạy hàng ngày vào lúc nửa đêm
    public void updatePromotionStatus() {
        System.out.println("Updating promotion statuses...");
        LocalDateTime today = LocalDateTime.now();
        List<Promotion> promotions = promotionRepo.findAll();

        for (Promotion promotion : promotions) {
            if (promotion.getStartDate().isAfter(today)) {
                promotion.setStatus((byte) 0); // Chưa bắt đầu
            } else if (promotion.getEndDate().isBefore(today)) {
                promotion.setStatus((byte) 2); // Đã kết thúc
                // Xóa quan hệ với sản phẩm trong PromotionProduct nếu đã kết thúc
                productPromotionRepo.updateStatusByPromotionId(promotion.getId(),(byte)0);
                productRepo.clearPromotionByPromotionId(promotion.getId());
            } else {
                promotion.setStatus((byte) 1); // Đang diễn ra
                // Thêm hoặc cập nhật các sản phẩm liên kết với promotion trong PromotionProduct
                List<ProductPromotion> promotionProducts = productPromotionRepo.findAllByPromotion(promotion);

                for (ProductPromotion promotionProduct : promotionProducts) {
                    Product product = promotionProduct.getProduct();
                    product.setPromotion(promotion);  // set thông tin khuyến mãi hiện tại
                    productRepo.save(product);  // Cập nhật product nếu có thay đổi

                }
            }
        }
        promotionRepo.saveAll(promotions);
    }
    public List<Promotion> getAllPromotions() {
        return promotionRepo.findAll();
    }
}
