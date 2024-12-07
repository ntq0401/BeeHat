package com.beehat.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @NotBlank(message = "Username không được để trống")
    @Size(max = 100, message = "Username phải có độ dài dưới 100 ký tự")
    private String username;

    @NotBlank(message = "Password không được để trống")
    @Size(max = 100, message = "Password phải có độ dài dưới 100 ký tự")
    private String password;

    @NotNull(message = "Fullname không được để trống")
    @Size(max = 100, message = "Fullname không được vượt quá 100 ký tự")
    private String fullname;

    @Email(message = "Email không hợp lệ")
    @NotBlank(message = "Email không được để trống")
    @Size(max = 100, message = "Fullname không được vượt quá 100 ký tự")
    private String email;

    @Size(max = 255, message = "Address không được vượt quá 255 ký tự")
    private String address;

    private String city;

    private String district;

    private String ward;

    @Size(max = 100, message = "Country không được vượt quá 100 ký tự")
    private String country;

    @Size(max = 500, message = "Photo URL không được vượt quá 500 ký tự")
    private String photo;

    private String phone;

    @Column(insertable = false)
    private Byte status;

    @Column(name = "created_date", insertable = false, updatable = false)
    private LocalDateTime createdDate;
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
    // Các trường tạm thời để lưu tên của tỉnh, huyện, xã (Không cần lưu vào cơ sở dữ liệu)
    @Transient
    private String provinceName;

    @Transient
    private String districtName;

    @Transient
    private String wardName;
    @PreUpdate
    public void preUpdate() {
        updatedDate = LocalDateTime.now();
    }
    public Customer( String username, String password, String fullname, String email, String address, String city, String district, String ward, String country, String photo, String phone, Byte status) {
        this.username = username;
        this.password = password;
        this.fullname = fullname;
        this.email = email;
        this.address = address;
        this.city = city;
        this.district = district;
        this.ward = ward;
        this.country = country;
        this.photo = photo;
        this.phone = phone;
        this.status = status;
    }
    public String allAddress() {
        return address + "," + city + "," + district + "," + ward;
    }
}

