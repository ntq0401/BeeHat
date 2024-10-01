package com.beehat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "order")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne @JoinColumn(name = "payment_method_id")
    private PaymentMethod paymentMethod;

    @Column(name = "deliver_date")
    private LocalDateTime deliverDate;

    @Column(name = "total_price")
    private Integer totalPrice;

    @ManyToOne @JoinColumn(name = "voucher_id")
    private Voucher voucher;

    @Column(name = "voucher_discount")
    private Integer voucherDiscount;

    @Column(name = "final_price")
    private Integer finalPrice;

    @Column(name = "shipping_address")
    private String shippingAddress;

    @Column(name = "shipping_city")
    private String shippingCity;

    @Column(name = "shipping_district")
    private String shippingDistrict;

    @Column(name = "shipping_ward")
    private String shippingWard;

    @Column(name = "shipping_country")
    private String shippingCountry;

    @Column(name = "phone")
    private String phone;

    @Column(name = "order_tracking_number")
    private String orderTrackingNumber; // Số theo dõi đơn hàng

    @Column(name = "order_status")
    private Byte orderStatus; // Trạng thái đơn hàng

    private Byte status;

    @Column(name = "created_date")
    private LocalDateTime createdDate; // Ngày tạo

    @Column(name = "updated_date")
    private LocalDateTime updatedDate; // Ngày cập nhật
}
