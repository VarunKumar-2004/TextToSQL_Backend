package com.project.TextToSQL.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatabaseCahceCleanup {

    private final DatabaseSessionManager databaseSessionManager;

    @Scheduled(fixedRate = 15 * 60 * 1000)
    public void cleanup() {

        /*
         * Remove databases that have not been accessed
         * for 60 minutes.
         */
        databaseSessionManager.cleanupInactiveDatabases(60);
    }
}