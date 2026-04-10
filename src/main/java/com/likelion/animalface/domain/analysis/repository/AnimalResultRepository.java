package com.likelion.animalface.domain.analysis.repository;

import com.likelion.animalface.domain.analysis.entity.AnimalResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnimalResultRepository extends JpaRepository<AnimalResult, Long> {

    Page<AnimalResult> findByUserId(Long userId, Pageable pageable);
}
