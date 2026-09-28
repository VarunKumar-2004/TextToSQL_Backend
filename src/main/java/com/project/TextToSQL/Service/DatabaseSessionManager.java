package com.project.TextToSQL.Service;

import com.project.TextToSQL.Model.ChatSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class DatabaseSessionManager {
    private final RestClient.Builder restClientBuilder;
    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.secret-key}")
    private String supabaseSecretKey;

    @Value("${supabase.storage.bucket}")
    private String bucketName;
    /*
     * Runtime cache.
     *
     * Key   -> ChatSession ID
     * Value -> Cached SQLite database
     */
    private final Map<UUID, CachedDatabase> databaseCache = new ConcurrentHashMap<>();
    private final Path rootDirectory =
            Path.of(System.getProperty("java.io.tmpdir"), "texttosql");
    public Path getDatabase(ChatSession session)throws IOException{
        CachedDatabase cachedDatabase=databaseCache.get(session.getId());
        if (cachedDatabase != null
                && Files.exists(cachedDatabase.path())) {

            cachedDatabase.updateLastAccessed();

            return cachedDatabase.path();
        }
        return downloadAndCache(session);
    }
    private Path downloadAndCache(ChatSession session)throws IOException{
        Path sessionDirectory=rootDirectory.resolve(session.getId().toString());
        Files.createDirectories(sessionDirectory);
        String fileName=extractFileName(session.getDatabaseFilePath());
        Path localDatabase =
                sessionDirectory.resolve(fileName);
        String downloadUrl =
                supabaseUrl
                        + "/storage/v1/object/"
                        + bucketName
                        + "/"
                        + session.getDatabaseFilePath();
        RestClient restClient =
                restClientBuilder.build();

        byte[] databaseBytes =
                restClient.get()
                        .uri(downloadUrl)
                        .header(
                                "Authorization",
                                "Bearer " + supabaseSecretKey
                        )
                        .header(
                                "apikey",
                                supabaseSecretKey
                        )
                        .retrieve()
                        .body(byte[].class);
        if (databaseBytes == null || databaseBytes.length == 0) {
            throw new IOException(
                    "Downloaded database file is empty"
            );
        }
        Files.write(
                localDatabase,
                databaseBytes
        );
        CachedDatabase cachedDatabase =
                new CachedDatabase(localDatabase);

        databaseCache.put(
                session.getId(),
                cachedDatabase
        );

        return localDatabase;
    }
    private String extractFileName(String storagePath) {

        int lastSlash =
                storagePath.lastIndexOf('/');

        if (lastSlash == -1) {
            return storagePath;
        }

        return storagePath.substring(lastSlash + 1);
    }
    public void cleanupInactiveDatabases(
            long inactiveMinutes
    ) {
        Instant cutoff =
                Instant.now()
                        .minusSeconds(
                                inactiveMinutes * 60
                        );
        databaseCache.entrySet().removeIf(entry -> {
            CachedDatabase cachedDatabase =
                    entry.getValue();

            if (cachedDatabase.lastAccessed()
                    .isBefore(cutoff)) {

                deleteDatabase(
                        entry.getKey(),
                        cachedDatabase.path()
                );

                return true;
            }

            return false;
        });
    }
    /**
     * Deletes a cached SQLite database.
     */
    private void deleteDatabase(
            UUID sessionId,
            Path databasePath
    ) {
        try {
            Files.deleteIfExists(databasePath);
            Path sessionDirectory =
                    databasePath.getParent();
            if (sessionDirectory != null) {
                Files.deleteIfExists(sessionDirectory);
            }
        } catch (IOException e) {
            // Log later using proper logger
            System.err.println(
                    "Failed to delete cached database for session "
                            + sessionId
                            + ": "
                            + e.getMessage()
            );
        }
    }
    public void removeSession(UUID sessionId) {

        CachedDatabase cachedDatabase =
                databaseCache.remove(sessionId);

        if (cachedDatabase != null) {

            deleteDatabase(
                    sessionId,
                    cachedDatabase.path()
            );
        }
    }
    private static class CachedDatabase {

        private final Path path;

        private volatile Instant lastAccessed;

        public CachedDatabase(Path path) {

            this.path = path;

            this.lastAccessed =
                    Instant.now();
        }

        public Path path() {
            return path;
        }

        public Instant lastAccessed() {
            return lastAccessed;
        }

        public void updateLastAccessed() {
            this.lastAccessed =
                    Instant.now();
        }
    }
}
