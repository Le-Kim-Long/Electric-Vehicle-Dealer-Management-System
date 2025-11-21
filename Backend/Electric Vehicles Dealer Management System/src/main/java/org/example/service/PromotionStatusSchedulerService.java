package org.example.service;

public interface PromotionStatusSchedulerService {

    /**
     * Runs when the server starts up to update all promotion statuses
     */
    void updatePromotionStatusesOnStartup();

    /**
     * Scheduled to run every 24 hours at midnight (00:00:00)
     * Cron expression: "0 0 0 * * ?" means every day at 00:00:00
     */
    void updatePromotionStatusesDaily();
}
