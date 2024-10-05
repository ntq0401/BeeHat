package com.beehat.repository;

import com.beehat.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepo extends JpaRepository<Product, Integer> {
    @Query("select p from Product p where p.name like ?1")
    public List<Product> findByName(String keyword);
}
