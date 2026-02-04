package com.dev.batchpractice.repository;

import com.dev.batchpractice.entity.BatchOutput;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BatchOutputRepository extends JpaRepository<BatchOutput, Long> {
}
