package com.project.TextToSQL.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class DatabaseStorageService {
    private final RestClient restClient;
    @Value("${supabase.url}")
    private String supabaseUrl;
    @Value("${supabase.secret-key}")
    private String supabaseSecret;
    @Value("${supabase.storage.bucket}")
    private String bucketName;
    public DatabaseStorageService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }
    public String uploadDatabase(MultipartFile file, UUID userId,UUID sessionId)throws IOException {
        String originalFileName=file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new IllegalArgumentException("File name is missing");
        }
        String storagePath =
                userId + "/" +
                        sessionId + "/" +
                        originalFileName;
        String uploadUrl =
                supabaseUrl +
                        "/storage/v1/object/" +
                        bucketName +
                        "/" +
                        storagePath;
        ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return originalFileName;
            }
        };
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + supabaseSecret);
        headers.set("apikey", supabaseSecret);
        headers.setContentType(
                MediaType.parseMediaType(
                        file.getContentType() != null
                                ? file.getContentType()
                                : "application/octet-stream"
                )
        );

        restClient.put()
                .uri(uploadUrl)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .body(resource)
                .retrieve()
                .toBodilessEntity();
        return storagePath;
    }
}
