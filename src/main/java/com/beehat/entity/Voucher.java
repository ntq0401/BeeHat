package com.beehat.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "voucher")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Voucher {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Không được bỏ trống tên voucher !")
    @Size(max = 50, message = "Tên không được vượt quá 50 kí tự")
    private String name;

    @NotBlank(message = "Không được bỏ trống mã code !")
    @Size(max = 50, message = "Mã không được vượt quá 50 kí tự")
    @Column(unique = true)
    private String code;

    @NotNull(message = "Không được bỏ trống số lượng !")
    @Min(value = 0, message = "Số lượng không được âm")
    private Integer quantity;

    @NotBlank(message = "Không được bỏ trống mô tả !")
    @Size(max = 255, message = "Mô tả không được vượt quá 255 kí tự")
    private String description;
    @NotNull(message = "Không được bỏ trống % giảm giá !")
    @Min(value = 0, message = "Phần trăm giảm giá không được âm")
    @Max(value = 70, message = "Phần trăm giảm giá không được vượt quá 70%")
    @Column(name = "discount_percentage")
    private Byte discountPercentage;

    @Column(name = "discount_amount", insertable = false, updatable = false)
    private Integer discountAmount;
    @NotNull(message = "Không được bỏ trống giá trị giảm giá tối đa !")
    @Min(value = 0, message = "Giá trị giảm tối đa không được âm")
    @Column(name = "discount_max")
    private Integer discountMax;

    @NotNull(message = "Ngày bắt đầu không được bỏ trống")
    private LocalDateTime startDate;

    @NotNull(message = "Ngày kết thúc không được bỏ trống")
    private LocalDateTime endDate;

    @AssertTrue(message = "Ngày kết thúc phải sau ngày bắt đầu")
    public boolean isEndDateAfterStartDate() {
        return startDate != null && endDate != null && endDate.isAfter(startDate);
    }

    @Column(name = "min_order_value")
    @NotNull(message = "Không được bỏ trống giá trị đơn hàng tối thiểu!")
    @Min(value = 0, message = "Giá trị đơn hàng tối thiểu không được âm")
    private Integer minOrderValue;

    @Column(insertable = false)
    private Byte status;

    @Column(name = "created_date", insertable = false, updatable = false)
    private LocalDateTime createdDate;
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
    @PreUpdate
    public void preUpdate() {
        updatedDate = LocalDateTime.now();
    }
}
