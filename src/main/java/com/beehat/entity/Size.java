package com.beehat.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "size")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Size {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @NotBlank(message = "Tên không được bỏ trống !")
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull(message = "Trạng thái không được bỏ trống !")
    @Min(value = 0, message = "Status can only be 0 or 1")
    @Max(value = 1, message = "Status can only be 0 or 1")
    @Column(name = "status", nullable = false)
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
