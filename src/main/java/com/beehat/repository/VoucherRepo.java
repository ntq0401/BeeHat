package com.beehat.repository;

import com.beehat.entity.Invoice;
import com.beehat.entity.Voucher;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public interface VoucherRepo extends JpaRepository<Voucher, Integer> {
    List<Voucher> findByStatus(byte status);
    List<Voucher> findByStatusAndEndDate(byte status, LocalDateTime endDate);
    Voucher findByCode(String code);
    @Query("SELECT v FROM Voucher v WHERE v.startDate <= :currentDate AND v.endDate >= :currentDate AND v.quantity > 0 AND v.status = 1")
    List<Voucher> findAvailableVouchers(@Param("currentDate") LocalDateTime currentDate);
    boolean existsByCodeAndIdNot(String code, Integer id);
    boolean existsByCode(@NotBlank(message = "Không được bỏ trống mã code !") @Size(max = 50, message = "Mã không được vượt quá 50 kí tự") String code);
    @Query("SELECT v FROM Voucher v " +
            "WHERE (:searchTerm IS NULL OR " +
            "       LOWER(v.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "       LOWER(v.code) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " + // Đảm bảo OR đúng chỗ
            "AND (:status IS NULL OR v.status = :status) " +
            "AND (:startDate IS NULL OR v.createdDate >= :startDate) " +
            "AND (:endDate IS NULL OR v.endDate <= :endDate)")
    Page<Voucher> searchVouchers(@Param("searchTerm") String searchTerm,
                                 @Param("status") Byte status,
                                 @Param("startDate") LocalDateTime startDate,
                                 @Param("endDate") LocalDateTime endDate, Pageable pageable);

}
