package com.resumematcher.backend.repository;

import com.resumematcher.backend.model.MatchResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchResultRepository extends JpaRepository<MatchResult, Long> {
    List<MatchResult> findTop50ByOrderByAnalyzedAtDesc();
}
