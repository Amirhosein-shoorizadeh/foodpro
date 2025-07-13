package service;

import dao.TokenDao;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TokenCleanupService {

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public static void start() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                TokenDao.deleteExpiredAndRevokedTokens();
                System.out.println(" Token cleanup job ran successfully.");
                System.out.println(" Token cleanup job ran complete."+ LocalDateTime.now());
            } catch (Exception e) {
                System.err.println("⚠️ Error while cleaning up tokens: " + e.getMessage());
            }
        }, 0, 30, TimeUnit.MINUTES); // هر 30 دقیقه
    }

    public static void shutdown() {
        scheduler.shutdown();
    }
}
