package com.beehat.repository;

import com.beehat.entity.Lining;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LiningRepo extends JpaRepository<Lining, Integer> {
}
