package com.beehat.service;

import com.beehat.entity.*;
import com.beehat.repository.CartDetailRepo;
import com.beehat.repository.ProductDetailRepo;
import com.beehat.repository.VoucherRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@SessionScope
public class CartService {
    @Autowired
    ProductDetailRepo productDetailRepo;
    @Autowired
    CartDetailRepo cartDetailRepo;
    @Autowired
    VoucherRepo voucherRepo;
    private List<CartDetail> cartDetails = new ArrayList<>();
    private Invoice temporaryInvoice;

    public List<CartDetail> getCartDetails() {
        return cartDetails;
    }

    public Invoice getTemporaryInvoice() {
        return temporaryInvoice;
    }
    public void addAllCart(List<CartDetail> newCartDetails) {
        cartDetails.clear();
        this.cartDetails = newCartDetails;
    }
    public void createTemporaryInvoice(Customer customer,List<CartDetail> carts) {
        Invoice invoice = new Invoice();
        invoice.setInvoiceTrackingNumber(generateInvoiceTrackingNumber());
        invoice.setCustomer(customer);
        invoice.setInvoiceStatus((byte) 1);
        invoice.setTotalPrice(carts.stream().mapToInt(cart -> cart.getProductDetail().getPrice() * cart.getQuantity()).sum());
        invoice.setFinalPrice(carts.stream().mapToInt(cart -> {
            int price = cart.getProductDetail().getPrice();
            if (cart.getProductDetail().getProduct().getPromotion() != null) {
                Promotion promotion = cart.getProductDetail().getProduct().getPromotion();

                if (promotion.getDiscountPercentage() != null && promotion.getDiscountPercentage() != 0) {
                    // Giảm giá theo tỷ lệ phần trăm
                    int discountAmount = (promotion.getDiscountPercentage() * price) / 100;
                    price = price - discountAmount;
                } else if (promotion.getDiscountAmount() != null && promotion.getDiscountAmount() != 0) {
                    // Giảm giá theo số tiền cố định
                    price = price - promotion.getDiscountAmount();
                }

                // Đảm bảo giá không âm
                if (price < 0) price = 0;
            }
            return cart.getQuantity() * price;
        }).sum());
        int promotionDiscount = invoice.getTotalPrice() - invoice.getFinalPrice();
        invoice.setPromotionDiscount(promotionDiscount);

        this.temporaryInvoice = invoice;
    }



    public void clearTemporaryInvoice() {
        this.temporaryInvoice = null;
        this.cartDetails.clear();
    }
    public void add(int id,int quantity) {
        CartDetail cartDetail = cartDetails.
                stream()
                .filter(prd -> prd.getProductDetail().getId() == id)
                .findFirst().orElse(null);
        if (cartDetail != null) {
            cartDetail.setQuantity(cartDetail.getQuantity() + quantity);
            return;
        }
        ProductDetail productDetail = productDetailRepo.findById(id).orElse(null);
        if (productDetail != null) {
            cartDetails.add(
                    new CartDetail(null, productDetail, null, quantity, null, null, null, null, null, null, null)
            );
        }
    }

    public void remove(int id) {
        cartDetails = cartDetails
                .stream()
                .filter(it -> it.getProductDetail().getId() != id)
                .collect(Collectors.toList());
    }

    public void clear() {
        cartDetails.clear();
    }

    public void update(int id, int qty) {
        CartDetail item = cartDetails
                .stream()
                .filter(it -> it.getProductDetail().getId() == id)
                .findFirst()
                .orElse(null);

        if (item != null) {
            item.setQuantity(qty);
        }
    }

    public long getTotal() {
        long total = cartDetails.stream()
                .map(CartDetail::getProductDetail)  // Lấy ra đối tượng ProductDetail từ mỗi CartDetail
                .distinct()  // Loại bỏ sản phẩm trùng lặp (nếu cần)
                .count();  // Đếm số lượng các sản phẩm
        return total;
    }

    public int getAmount() {
        int amount = 0;
        for (CartDetail item : cartDetails) {
            amount += item.getQuantity() * item.getProductDetail().getPrice();
        }
        return amount;
    }

    public void mergeCartWithUserCart(Customer customer) {
        // Giỏ hàng tạm thời trong session
        List<CartDetail> tempCart = getCartDetails();
        // Nếu không có sản phẩm nào trong giỏ hàng tạm thời, không cần gộp
        if (tempCart == null || tempCart.isEmpty()) {
            return;
        }
        // Giỏ hàng của người dùng đã đăng nhập (từ cơ sở dữ liệu)
        List<CartDetail> userCart = cartDetailRepo.findByCustomerIdAndStatus(customer.getId(), (byte) 1);

        // Gộp giỏ hàng: nếu sản phẩm đã có trong giỏ hàng người dùng, cộng số lượng lại
        for (CartDetail tempCartDetail : tempCart) {
            boolean found = false;
            for (CartDetail userCartDetail : userCart) {
                if (userCartDetail.getProductDetail().getId().equals(tempCartDetail.getProductDetail().getId())) {
                    // Kiểm tra số lượng trong kho
                    int availableStock = productDetailRepo.findById(tempCartDetail.getProductDetail().getId()).get().getStock(); // Lấy số lượng trong kho
                    // Tính tổng số lượng hiện tại
                    int currentQuantityInCart = userCartDetail.getQuantity();
                    int quantityToAdd = tempCartDetail.getQuantity();

                    // Kiểm tra số lượng tối đa có thể thêm vào
                    int maxAddable = availableStock - currentQuantityInCart;
                    if (maxAddable > 0) {
                        int quantityToMerge = Math.min(maxAddable, quantityToAdd);
                        userCartDetail.setQuantity(currentQuantityInCart + quantityToMerge);
                    }

                    // Nếu không đủ số lượng, bỏ qua sản phẩm này
                    found = true;
                    break;
                }
            }

            // Nếu sản phẩm chưa có trong giỏ hàng người dùng, thêm mới vào giỏ hàng người dùng
            if (!found) {
                int availableStock = productDetailRepo.findById(tempCartDetail.getProductDetail().getId()).get().getStock(); // Lấy số lượng trong kho
                int quantityToAdd = tempCartDetail.getQuantity();

                if (quantityToAdd <= availableStock) {
                    userCart.add(tempCartDetail);
                } else if (availableStock > 0) {
                    // Nếu số lượng trong kho không đủ, chỉ thêm số lượng tối đa có thể
                    tempCartDetail.setQuantity(availableStock);
                    userCart.add(tempCartDetail);
                }
            }
        }

        // Cập nhật giỏ hàng của người dùng vào cơ sở dữ liệu
        for (CartDetail cartDetail : userCart) {
            cartDetail.setCustomer(customer);  // Đảm bảo gán đúng người dùng
            cartDetailRepo.save(cartDetail);
        }

        // Xóa giỏ hàng tạm thời trong session (nếu cần)
        clear();
    }
    private String generateInvoiceTrackingNumber() {
        // Sinh chuỗi ngẫu nhiên 5 ký tự
        String randomUUID = UUID.randomUUID().toString().substring(0, 5).toUpperCase();

        // Thời gian hiện tại dưới dạng yyyyMMddHHmmss
        String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMddHHmmss"));

        // Tạo SKU bằng cách kết hợp productCode, timeStamp, và randomUUID
        return randomUUID + "-" + timeStamp;
    }
}
