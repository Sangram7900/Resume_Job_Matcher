package com.resumematcher.backend.controller;

import com.resumematcher.backend.dto.MatchRequest;
import com.resumematcher.backend.service.AiServiceClient;
import com.resumematcher.backend.service.MatchPersistenceService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class MatchController {
    private final AiServiceClient aiServiceClient;
    private final MatchPersistenceService persistenceService;

    public MatchController(AiServiceClient aiServiceClient, MatchPersistenceService persistenceService) {
        this.aiServiceClient = aiServiceClient;
        this.persistenceService = persistenceService;
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() { return ResponseEntity.ok("{\"status\":\"ok\"}"); }

    @PostMapping(value="/match", consumes=MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> match(@RequestBody MatchRequest request) {
        String result = aiServiceClient.matchText(request.getResume_text(), request.getJob_description());
        persistenceService.save("text-resume", request.getResume_text(), request.getJob_description(), result, null, "text/plain");
        return ResponseEntity.ok(result);
    }

    @PostMapping(value="/match-file", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> matchFile(@RequestPart("resume") MultipartFile resume, @RequestPart("job_description") String jobDescription) throws java.io.IOException {
        String result = aiServiceClient.matchFile(resume, jobDescription);
        persistenceService.save(resume.getOriginalFilename(), "", jobDescription, result, resume.getBytes(), resume.getContentType());
        return ResponseEntity.ok(result);
    }
}

