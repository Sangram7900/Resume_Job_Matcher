package com.resumematcher.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class AiServiceClient {
    private final String aiServiceUrl;
    public AiServiceClient(@Value("${ai.service.url}") String aiServiceUrl) { this.aiServiceUrl = aiServiceUrl; }

    public String matchText(String resumeText, String jobDescription) {
        try {
            URL url = new URL(aiServiceUrl + "/api/ai/match");
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("POST"); c.setDoOutput(true);
            c.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            c.setRequestProperty("Accept", "application/json");
            String json = "{\"resume_text\":\"" + escapeJson(resumeText) + "\",\"job_description\":\"" + escapeJson(jobDescription) + "\"}";
            byte[] body = json.getBytes(StandardCharsets.UTF_8);
            c.setFixedLengthStreamingMode(body.length);
            try (OutputStream out = c.getOutputStream()) { out.write(body); }
            return readResponse(c);
        } catch (Exception e) { throw new RuntimeException("Could not connect to AI service: " + e.getMessage(), e); }
    }

    public String matchFile(MultipartFile resume, String jobDescription) {
        String boundary = "----ResumeMatcher" + UUID.randomUUID();
        try {
            URL url = new URL(aiServiceUrl + "/api/ai/match-file");
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("POST"); c.setDoOutput(true);
            c.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            c.setRequestProperty("Accept", "application/json");
            try (OutputStream out = new BufferedOutputStream(c.getOutputStream())) {
                writeTextPart(out, boundary, "job_description", jobDescription == null ? "" : jobDescription);
                writeFilePart(out, boundary, "resume", resume.getOriginalFilename() == null ? "resume" : resume.getOriginalFilename(), resume.getContentType() == null ? "application/octet-stream" : resume.getContentType(), resume.getBytes());
                out.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
            }
            return readResponse(c);
        } catch (Exception e) { throw new RuntimeException("Could not upload resume to AI service: " + e.getMessage(), e); }
    }

    private void writeTextPart(OutputStream out, String boundary, String name, String value) throws IOException {
        String header = "--" + boundary + "\r\n" + "Content-Disposition: form-data; name=\"" + name + "\"\r\n" + "Content-Type: text/plain; charset=UTF-8\r\n\r\n";
        out.write(header.getBytes(StandardCharsets.UTF_8)); out.write(value.getBytes(StandardCharsets.UTF_8)); out.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    private void writeFilePart(OutputStream out, String boundary, String name, String filename, String contentType, byte[] data) throws IOException {
        String safeFilename = filename.replace("\"", "");
        String header = "--" + boundary + "\r\n" + "Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + safeFilename + "\"\r\n" + "Content-Type: " + contentType + "\r\n\r\n";
        out.write(header.getBytes(StandardCharsets.UTF_8)); out.write(data); out.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    private String readResponse(HttpURLConnection c) throws IOException {
        int status = c.getResponseCode();
        InputStream stream = status >= 400 ? c.getErrorStream() : c.getInputStream();
        String response = stream == null ? "" : new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        if (status >= 400) throw new RuntimeException("AI service returned HTTP " + status + ": " + response);
        return response;
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "\\r").replace("\n", "\\n").replace("\t", "\\t");
    }
}
