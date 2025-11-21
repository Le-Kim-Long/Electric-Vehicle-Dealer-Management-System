package org.example.service.implementation;

import org.example.entity.Promotion;
import org.example.repository.PromotionRepository;
import org.example.service.PromotionStatusSchedulerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.List;

@Service
public class PromotionStatusSchedulerServiceImpl implements PromotionStatusSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(PromotionStatusSchedulerServiceImpl.class);

    @Autowired
    private PromotionRepository promotionRepository;

    /**
     * Runs when the server starts up to update all promotion statuses
     */
    @PostConstruct
    public void updatePromotionStatusesOnStartup() {
        logger.info("Starting promotion status update on server startup...");
        performStatusUpdate();
        logger.info("Completed promotion status update on server startup.");
    }

    /**
     * Scheduled to run every 24 hours at midnight (00:00:00)
     * Cron expression: "0 0 0 * * ?" means every day at 00:00:00
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void updatePromotionStatusesDaily() {
        logger.info("Starting daily promotion status update...");
        performStatusUpdate();
        logger.info("Completed daily promotion status update.");
    }

    /**
     * Core method to update all promotion statuses based on current date logic
     */
    private void performStatusUpdate() {
        try {
            LocalDate currentDate = LocalDate.now();
            logger.info("Updating promotion statuses for date: {}", currentDate);

            // Get all promotions from database
            List<Promotion> allPromotions = promotionRepository.findAll();

            if (allPromotions.isEmpty()) {
                logger.info("No promotions found in database.");
                return;
            }

            int updatedCount = 0;
            int activeCount = 0;
            int inactiveCount = 0;

            for (Promotion promotion : allPromotions) {
                String oldStatus = promotion.getStatus();
                String newStatus = calculatePromotionStatus(currentDate, promotion.getStartDate(), promotion.getEndDate());

                // Only update if status has changed
                if (!newStatus.equals(oldStatus)) {
                    promotion.setStatus(newStatus);
                    promotionRepository.save(promotion);
                    updatedCount++;

                    logger.debug("Updated promotion ID {} ({}): {} -> {}",
                        promotion.getPromotionId(),
                        promotion.getPromotionName(),
                        oldStatus,
                        newStatus);
                }

                // Count status types for reporting
                if ("Đang hoạt động".equals(newStatus)) {
                    activeCount++;
                } else {
                    inactiveCount++;
                }
            }

            logger.info("Promotion status update completed. Total: {}, Updated: {}, Active: {}, Inactive: {}",
                allPromotions.size(), updatedCount, activeCount, inactiveCount);

        } catch (Exception e) {
            logger.error("Error occurred while updating promotion statuses: {}", e.getMessage(), e);
        }
    }

    /**
     * Calculate promotion status based on current date and promotion date range
     * Logic: If current date is within [startDate, endDate) then "Đang hoạt động", else "Không hoạt động"
     */
    private String calculatePromotionStatus(LocalDate currentDate, LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return "Không hoạt động";
        }

        // Check if current date is within promotion period
        // (inclusive of start date, exclusive of end date)
        if ((currentDate.isEqual(startDate) || currentDate.isAfter(startDate)) &&
            currentDate.isBefore(endDate)) {
            return "Đang hoạt động";
        } else {
            return "Không hoạt động";
        }
    }
}
