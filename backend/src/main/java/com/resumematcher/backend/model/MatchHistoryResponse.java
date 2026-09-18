package com.resumematcher.backend.model;

import java.time.LocalDateTime;

public class MatchHistoryResponse {

    private Long id;
    private String resumeName;
    private String jobDescription;
    private double overallScore;
    private double semanticScore;
    private double tfidfScore;
    private double skillScore;
    private String matchedSkills;
    private String missingSkills;
    private String contentType;
    private LocalDateTime analyzedAt;

    public MatchHistoryResponse() {}

    public MatchHistoryResponse(MatchResult result) {
        this.id = result.getId();
        this.resumeName = result.getResumeName();
        this.jobDescription = result.getJobDescription();
        this.overallScore = result.getOverallScore();
        this.semanticScore = result.getSemanticScore();
        this.tfidfScore = result.getTfidfScore();
        this.skillScore = result.getSkillScore();
        this.matchedSkills = result.getMatchedSkills();
        this.missingSkills = result.getMissingSkills();
        this.contentType = result.getContentType();
        this.analyzedAt = result.getAnalyzedAt();
    }

    public Long getId() { return id; }
    public String getResumeName() { return resumeName; }
    public String getJobDescription() { return jobDescription; }
    public double getOverallScore() { return overallScore; }
    public double getSemanticScore() { return semanticScore; }
    public double getTfidfScore() { return tfidfScore; }
    public double getSkillScore() { return skillScore; }
    public String getMatchedSkills() { return matchedSkills; }
    public String getMissingSkills() { return missingSkills; }
    public String getContentType() { return contentType; }
    public LocalDateTime getAnalyzedAt() { return analyzedAt; }
}
