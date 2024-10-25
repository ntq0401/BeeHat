package com.beehat.repo;

import com.beehat.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepo extends JpaRepository<Customer, Integer> {
    @Query("SELECT e FROM Customer e WHERE e.id = :id1")
    Customer findeById(@Param("id1") Integer id);
    @Query("SELECT e FROM Customer e WHERE e.username = :username")
    Customer findByUsername(@Param("username") String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
}
