package com.beehat.service;

import com.beehat.entity.Invoice;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VNPayService {
    private static final Logger logger = LoggerFactory.getLogger(VNPayService.class);

    @Value("${vnpay.tmn_code}")
    private String vnpTmnCode;

    @Value("${vnpay.secret_key}")
    private String vnpSecretKey;

    @Value("${vnpay.payment_url}")
    private String vnpPaymentUrl;

    @Value("${vnpay.return_url}")
    private String vnpReturnUrl;

    // Tạo URL thanh toán VNPay
    public String createPaymentUrl(Invoice temporaryInvoice,HttpServletRequest request) {
        logger.info("Creating payment URL for invoice: {}", temporaryInvoice);

        // Các tham số bắt buộc cho VNPay
        String vnpVersion = "2.1.0";
        String vnpCommand = "pay";
        String vnpOrderInfo = "Thanh toan hoa don #" + temporaryInvoice.getInvoiceTrackingNumber();
        String vnpTxnRef = temporaryInvoice.getInvoiceTrackingNumber();
        String vnpAmount = String.valueOf(temporaryInvoice.getFinalPrice() * 100); // VNPay yêu cầu đơn vị là đồng
        String vnpIpAddr = getClientIp(request); // IP người dùng, có thể thay đổi tùy vào môi trường thực tế
        String vnpLocale = "vn";
        String vnpCurrCode = "VND";
        String vnpOrderType = "1";

        // Tạo Map chứa các tham số
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", vnpVersion);
        params.put("vnp_Command", vnpCommand);
        params.put("vnp_TmnCode", vnpTmnCode);
        params.put("vnp_Amount", vnpAmount);
        params.put("vnp_CurrCode", vnpCurrCode);
        params.put("vnp_TxnRef", vnpTxnRef);
        params.put("vnp_OrderInfo", urlEncode(vnpOrderInfo)); // Mã hóa URL
        params.put("vnp_Locale", vnpLocale);
        params.put("vnp_ReturnUrl", urlEncode(vnpReturnUrl));
        params.put("vnp_IpAddr", urlEncode(vnpIpAddr));
        params.put("vnp_OrderType", vnpOrderType);

        // Thêm thời gian tạo và hết hạn
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        params.put("vnp_CreateDate", now.format(formatter));
        params.put("vnp_ExpireDate", now.plusMinutes(15).format(formatter));

        logger.info("VNPay Parameters: {}", params);

        // Tạo chuỗi mã hóa và hash
        String query = params.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())  // Sắp xếp các tham số theo tên khóa
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));
        logger.info("Query string before signing: {}", query);

        // Tạo mã hash (hmacSHA512) cho chuỗi đã mã hóa
        String secureHash = hmacSHA512(vnpSecretKey, query);
        logger.info("Generated secure hash (local): {}", secureHash);
        params.put("vnp_SecureHash", secureHash);

        // Tạo URL thanh toán
        String paymentUrl = vnpPaymentUrl + "?" + params.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));

        // Log URL thanh toán
        logger.info("Generated VNPay payment URL: {}", paymentUrl);
        return paymentUrl;
    }

    // Kiểm tra và xác minh giao dịch trả về từ VNPay
    public boolean validateReturnUrl(Map<String, String> params) {
        logger.info("Received VNPay callback parameters: {}", params);
        String vnpSecureHash = params.get("vnp_SecureHash");
        logger.info("vnp_SecureHash from VNPay: {}", vnpSecureHash);
        params.remove("vnp_SecureHash");

        // Tạo chuỗi mã hóa từ các tham số đã loại bỏ vnp_SecureHash
        String query = params.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())  // Sắp xếp các tham số theo tên khóa
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));
        logger.info("Query string for hash validation: {}", query);

        // Tạo lại mã hash và kiểm tra
        String generatedHash = hmacSHA512(vnpSecretKey, query);
        logger.info("Generated hash (local): {}", generatedHash);
        boolean isValid = generatedHash.equals(vnpSecureHash);
        logger.info("Signature validation result: {}", isValid);

        return isValid;
    }

    // Hàm mã hóa HMAC-SHA512
    private String hmacSHA512(String key, String data) {
        try {
            Mac sha512_HMAC = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            sha512_HMAC.init(secretKey);
            byte[] hashBytes = sha512_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte b : hashBytes) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        } catch (Exception e) {
            logger.error("Error generating HMAC-SHA512 hash. Key: {}, Data: {}", key, data, e);
            return null;
        }
    }

    // Hàm mã hóa URL
    private String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            logger.error("Error encoding URL parameter: {}", value, e);
            return "";
        }
    }

    // Lấy IP của người dùng
    private String getClientIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }
}
