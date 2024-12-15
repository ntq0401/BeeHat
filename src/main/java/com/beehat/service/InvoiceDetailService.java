package com.beehat.service;


import com.beehat.DTO.ProductResponse;
import com.beehat.entity.InvoiceDetail;
import com.beehat.entity.Product;
import com.beehat.entity.ProductDetail;
import com.beehat.repository.InvoiceDetailRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceDetailService {
    @Autowired
    InvoiceDetailRepo invoiceDetailRepo;

    public List<ProductResponse> countSL() {
        List<InvoiceDetail> invoiceDetailList = invoiceDetailRepo.findAll();
        Map<Product, Integer> result = new HashMap<>();

        for (InvoiceDetail invoiceDetail : invoiceDetailList) {
            ProductDetail productDetail = invoiceDetail.getProductDetail();
            int quantity = invoiceDetail.getQuantity();

            if(invoiceDetail.getInvoice().getStatus() == 8 || invoiceDetail.getInvoice().getInvoiceStatus() == 0){
                if (productDetail != null && result.containsKey(productDetail.getProduct())) {
                    result.put(productDetail.getProduct(), result.get(productDetail.getProduct()) + quantity);
                } else {
                    result.put(productDetail.getProduct(), quantity);
                }
            }
        }

        List<ProductResponse> productResponseList = new ArrayList<>();

        for (Map.Entry<Product, Integer> entry : result.entrySet()) {
            ProductResponse productResponse = ProductResponse.builder()
                    .image(entry.getKey().getImages().get(0).getImageUrl())
                    .sl(entry.getValue())
                    .price(entry.getValue() * entry.getKey().getProductDetail().get(0).getPrice())
                    .name(entry.getKey().getName())
                    .build();
            productResponseList.add(productResponse);
        }

        productResponseList.sort((p1, p2) -> Integer.compare(p2.getSl(), p1.getSl()));

        return productResponseList;
    }
    public List<ProductResponse> getTop3BestSell() {
        List<InvoiceDetail> invoiceDetails = invoiceDetailRepo.findAll();
        Map<Product, Integer> result = new HashMap<>();

        // Tính tổng số lượng (quantity) cho từng sản phẩm
        for (InvoiceDetail invoiceDetail : invoiceDetails) {
            ProductDetail productDetail = invoiceDetail.getProductDetail();
            int quantity = invoiceDetail.getQuantity();

            // Chỉ tính những hóa đơn có status = 8 hoặc invoiceStatus = 0
            if (invoiceDetail.getInvoice().getStatus() == 8 || invoiceDetail.getInvoice().getInvoiceStatus() == 0) {
                if (productDetail != null && result.containsKey(productDetail.getProduct())) {
                    result.put(productDetail.getProduct(), result.get(productDetail.getProduct()) + quantity);
                } else {
                    result.put(productDetail.getProduct(), quantity);
                }
            }
        }

        // Chuyển Map sang danh sách ProductResponse
        List<ProductResponse> productResponseList = new ArrayList<>();

        for (Map.Entry<Product, Integer> entry : result.entrySet()) {
            ProductResponse productResponse = ProductResponse.builder()
                    .image(entry.getKey().getImages().get(0).getImageUrl()) // Hình ảnh đầu tiên
                    .sl(entry.getValue()) // Tổng số lượng bán ra
                    .price(entry.getKey().getProductDetail().get(0).getPrice()) // Tổng giá trị
                    .name(entry.getKey().getName()) // Tên sản phẩm
                    .lowestPrice(calculateLowestPrice(entry.getKey().getProductDetail()))
                    .highestPrice(calculateHighestPrice(entry.getKey().getProductDetail()))
                    .productDetail(entry.getKey().getProductDetail())
                    .status(entry.getKey().getStatus())
                    .build();
            productResponseList.add(productResponse);
        }

        // Sắp xếp danh sách theo số lượng bán ra giảm dần
        productResponseList.sort((p1, p2) -> Integer.compare(p2.getSl(), p1.getSl()));

        // Lấy Top 3 sản phẩm bán chạy nhất
        return productResponseList.stream()
                .limit(3) // Lấy top 3
                .collect(Collectors.toList());
    }

    private int calculateLowestPrice(List<ProductDetail> productDetails) {
        return productDetails.stream()
                .mapToInt(ProductDetail::getPrice)
                .min()
                .orElse(0);  // Default to 0 if no prices found
    }

    // Method to calculate highest price
    private int calculateHighestPrice(List<ProductDetail> productDetails) {
        return productDetails.stream()
                .mapToInt(ProductDetail::getPrice)
                .max()
                .orElse(0);  // Default to 0 if no prices found
    }

}
