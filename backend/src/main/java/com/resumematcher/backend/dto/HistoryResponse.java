package com.resumematcher.backend.dto;

import com.resumematcher.backend.model.MatchResult;

import java.time.LocalDateTime;

public record HistoryResponse(
        Long id,
        String resumeName,
        double overallScore,
        double semanticScore,
        double tfidfScore,
        double skillScore,
        String matchedSkills,
        String missingSkills,
        LocalDateTime analyzedAt
) {
    public static HistoryResponse from(MatchResult item) {
        return new HistoryResponse(
                item.getId(), item.getResumeName(), item.getOverallScore(), item.getSemanticScore(),
                item.getTfidfScore(), item.getSkillScore(), item.getMatchedSkills(),
                item.getMissingSkills(), item.getAnalyzedAt()
        );
    }
}
