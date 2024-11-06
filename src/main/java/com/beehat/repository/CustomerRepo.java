package com.beehat.repository;

import com.beehat.entity.Customer;
import com.beehat.entity.Employee;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerRepo extends JpaRepository<Customer, Integer> {
    @Query("SELECT e FROM Customer e WHERE e.id = :id1")
    Customer findeById(@Param("id1") Integer id);
    @Transactional
    @Query("SELECT e FROM Customer e WHERE e.username = :username")
    Customer findByUsername(@Param("username") String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    // Phương thức tìm tất cả khách hàng theo trạng thái và vai trò
    @Query("SELECT e FROM Customer e WHERE (:status IS NULL OR e.status = :status) ")
    List<Customer> findAllCustomers(@Param("status") Byte statusValue);

    // Phương thức tìm Khách hàng theo tên, username hoặc số điện thoại
    @Query("SELECT e FROM Customer e WHERE " +
            "(LOWER(e.fullname) LIKE LOWER(CONCAT('%', :searchValue, '%')) OR " +
            "LOWER(e.username) LIKE LOWER(CONCAT('%', :searchValue, '%')) OR " +
            "LOWER(e.phone) LIKE LOWER(CONCAT('%', :searchValue, '%'))) " +
            "AND (:status IS NULL OR e.status = :status)")
    List<Customer> findCustomersBySearchValue(@Param("searchValue") String searchValue,
                                              @Param("status") Byte statusValue);
}

