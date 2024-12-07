package com.beehat.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "employee")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Employee implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @NotBlank(message = "Username không được để trống")
    @Size(max = 100, message = "Username không được vượt quá 100 ký tự")
    private String username;

    @NotBlank(message = "Password không được để trống")
    @Size(max = 100, message = "Password phải có độ dài dưới 100 ký tự")
    private String password;

    @NotBlank(message = "Fullname không được để trống")
    @Size(max = 100, message = "Fullname không được vượt quá 100 ký tự")
    private String fullname;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Phone không được để trống")
    @Pattern(regexp = "\\d{10}", message = "Phone phải là chuỗi số và có đúng 10 ký tự")
    private String phone;

    @Size(max = 500, message = "Photo URL không được vượt quá 500 ký tự")
    private String photo;

    private Byte role;

    private Byte status;

    @Column(name = "created_date", insertable = false, updatable = false)
    private LocalDateTime createdDate;
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
    @PreUpdate
    public void preUpdate() {
        updatedDate = LocalDateTime.now();
    }

    public Employee(String username, String password, String fullname, String email, String phone, String photo, Byte role, Byte status) {
        this.username = username;
        this.password = password;
        this.fullname = fullname;
        this.email = email;
        this.phone = phone;
        this.photo = photo;
        this.role = role;
        this.status = status;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role == 1 ? "ROLE_ADMIN" : "ROLE_EMPLOYEE"));
    }
    @Override
    public boolean isEnabled() {
        return status == 1; // Chỉ cho phép những nhân viên có status = 1
    }
}
