package com.beehat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "employee")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String username;

    private String password;

    private String fullname;

    private String email;

    private String phone;

    private String photo;

    private Byte role;

    private Byte status;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

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
}
