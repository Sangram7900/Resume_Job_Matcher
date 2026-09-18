package com.resumematcher.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumematcher.backend.model.MatchResult;
import com.resumematcher.backend.repository.MatchResultRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MatchPersistenceService {
    private final MatchResultRepository repository;
    private final ObjectMapper objectMapper;

    public MatchPersistenceService(MatchResultRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public void save(String resumeName, String resumeText, String jobDescription, String aiJson, byte[] resumeFile, String contentType) {
        try {
            JsonNode root = objectMapper.readTree(aiJson);
            MatchResult result = new MatchResult();
            result.setResumeName(resumeName == null || resumeName.isBlank() ? "resume" : resumeName);
            result.setResumeText(resumeText == null ? root.path("resume_text").asText("") : resumeText);
            result.setJobDescription(jobDescription);
            result.setOverallScore(root.path("overall_score").asDouble(0));
            result.setSemanticScore(root.path("semantic_score").asDouble(0));
            result.setTfidfScore(root.path("tfidf_score").asDouble(0));
            result.setSkillScore(root.path("skill_score").asDouble(0));
            result.setMatchedSkills(toJsonArray(root.path("matched_skills")));
            result.setMissingSkills(toJsonArray(root.path("missing_skills")));
            result.setResumeFile(resumeFile);
            result.setContentType(contentType);
            result.setAnalyzedAt(LocalDateTime.now());
            repository.save(result);
        } catch (Exception e) {
            throw new RuntimeException("Could not save match result to PostgreSQL: " + e.getMessage(), e);
        }
    }

    private String toJsonArray(JsonNode node) {
        if (node == null || !node.isArray()) return "[]";
        List<String> values = new ArrayList<>();
        node.forEach(item -> values.add(item.asText()));
        try {
            return objectMapper.writeValueAsString(values);
        } catch (Exception e) {
            return "[]";
        }
    }
}
