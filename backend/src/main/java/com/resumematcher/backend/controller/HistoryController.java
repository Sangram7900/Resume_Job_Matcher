package com.resumematcher.backend.controller;

import com.resumematcher.backend.model.MatchHistoryResponse;
import com.resumematcher.backend.model.MatchResult;
import com.resumematcher.backend.repository.MatchResultRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    private final MatchResultRepository repository;

    public HistoryController(MatchResultRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<MatchHistoryResponse> getHistory() {
        return repository.findTop50ByOrderByAnalyzedAtDesc()
                .stream()
                .map(MatchHistoryResponse::new)
                .toList();
    }
}
