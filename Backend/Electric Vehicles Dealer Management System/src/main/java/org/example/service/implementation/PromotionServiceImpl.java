package org.example.service.implementation;

import org.example.dto.CreatePromotionRequest;
import org.example.dto.PromotionResponse;
import org.example.dto.UpdatePromotionRequest;
import org.example.entity.Promotion;
import org.example.entity.UserAccount;
import org.example.repository.PromotionRepository;
import org.example.repository.UserAccountRepository;
import org.example.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PromotionServiceImpl implements PromotionService {

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    /**
     * Lấy tất cả promotions của dealer mà user đang đăng nhập thuộc về
     */
    public List<PromotionResponse> getPromotionsByDealerManager(String email) {
        // Find user by email
        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // Check if user is a dealer manager and has a dealer
        if (user.getDealer() == null) {
            throw new RuntimeException("User is not associated with any dealer");
        }

        // Get all promotions for the dealer
        List<Promotion> promotions = promotionRepository.findByDealerId(user.getDealer().getDealerId());

        // Convert to response DTOs
        return promotions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy thông tin promotion theo ID cho DealerManager
     */
    public PromotionResponse getPromotionByIdForDealerManager(Integer promotionId, String email) {
        // Find user by email
        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // Check if user is a dealer manager and has a dealer
        if (user.getDealer() == null) {
            throw new RuntimeException("User is not associated with any dealer");
        }

        // Find promotion by ID
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new RuntimeException("Promotion not found with ID: " + promotionId));

        // Check if promotion belongs to the user's dealer
        if (!promotion.getDealer().getDealerId().equals(user.getDealer().getDealerId())) {
            throw new RuntimeException("Access denied. This promotion does not belong to your dealer.");
        }

        return convertToResponse(promotion);
    }

    /**
     * Utility method to calculate promotion status based on current date
     * This method is used consistently across create, update, and scheduled operations
     */
    private String calculatePromotionStatus(LocalDate currentDate, LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return "Không hoạt động";
        }

        // Check if current date is within promotion period (inclusive of start, exclusive of end)
        if ((currentDate.isEqual(startDate) || currentDate.isAfter(startDate)) &&
                currentDate.isBefore(endDate)) {
            return "Đang hoạt động";
        } else {
            return "Không hoạt động";
        }
    }

    /**
     * Cập nhật promotion cho DealerManager với logic xử lý ngày tháng
     */
    public PromotionResponse updatePromotionForDealerManager(Integer promotionId, UpdatePromotionRequest request, String email) {
        // Find user by email
        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // Check if user is a dealer manager and has a dealer
        if (user.getDealer() == null) {
            throw new RuntimeException("User is not associated with any dealer");
        }

        // Find promotion by ID
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new RuntimeException("Promotion not found with ID: " + promotionId));

        // Check if promotion belongs to the user's dealer
        if (!promotion.getDealer().getDealerId().equals(user.getDealer().getDealerId())) {
            throw new RuntimeException("Access denied. This promotion does not belong to your dealer.");
        }

        LocalDate currentDate = LocalDate.now();
        LocalDate promotionStartDate = promotion.getStartDate();

        // Update non-date fields if provided
        if (request.getPromotionName() != null && !request.getPromotionName().trim().isEmpty()) {
            promotion.setPromotionName(request.getPromotionName());
        }
        if (request.getDescription() != null) {
            promotion.setDescription(request.getDescription());
        }
        if (request.getValue() != null && request.getValue().compareTo(BigDecimal.ZERO) > 0) {
            promotion.setValue(request.getValue());
        }
        if (request.getType() != null && !request.getType().trim().isEmpty()) {
            promotion.setType(request.getType());
        }
        // Note: Status is not updated from request - it will be auto-calculated based on dates

        // Date update logic based on current date vs promotion start date
        boolean hasStarted = currentDate.isAfter(promotionStartDate) || currentDate.isEqual(promotionStartDate);

        if (hasStarted) {
            // Promotion has started - only allow updating end date
            if (request.getStartDate() != null) {
                throw new RuntimeException("Cannot update start date after promotion has started");
            }

            if (request.getEndDate() != null) {
                // Validate end date must be after current date and after start date
                if (request.getEndDate().isBefore(currentDate)) {
                    throw new RuntimeException("End date must be after current date when promotion has started");
                }
                if (request.getEndDate().isBefore(promotionStartDate) || request.getEndDate().isEqual(promotionStartDate)) {
                    throw new RuntimeException("End date must be after start date");
                }
                promotion.setEndDate(request.getEndDate());
            }
        } else {
            // Promotion hasn't started - allow updating both start and end dates
            if (request.getStartDate() != null) {
                // Validate start date must be after current date
                if (request.getStartDate().isBefore(currentDate)) {
                    throw new RuntimeException("Start date must be after current date (" + currentDate + ")");
                }
                promotion.setStartDate(request.getStartDate());
            }

            if (request.getEndDate() != null) {
                promotion.setEndDate(request.getEndDate());
            }

            // Validate date logic after updates
            if (promotion.getStartDate() != null && promotion.getEndDate() != null) {
                if (promotion.getStartDate().isAfter(promotion.getEndDate()) || promotion.getStartDate().isEqual(promotion.getEndDate())) {
                    throw new RuntimeException("End date must be after start date");
                }
            }
        }

        // Auto-update status based on current logic (same as create API)
        if (promotion.getStartDate() != null && promotion.getEndDate() != null) {
            String status = calculatePromotionStatus(currentDate, promotion.getStartDate(), promotion.getEndDate());
            promotion.setStatus(status);
        }

        // Save updated promotion
        Promotion updatedPromotion = promotionRepository.save(promotion);

        return convertToResponse(updatedPromotion);
    }

    /**
     * Tạo promotion mới cho DealerManager
     */
    public PromotionResponse createPromotionForDealerManager(CreatePromotionRequest request, String email) {
        // Find user by email
        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // Check if user is a dealer manager and has a dealer
        if (user.getDealer() == null) {
            throw new RuntimeException("User is not associated with any dealer");
        }

        // Validate dates
        LocalDate currentDate = LocalDate.now();

        // Start date must be after current date
        if (request.getStartDate().isBefore(currentDate)) {
            throw new RuntimeException("Start date must be after current date (" + currentDate + ")");
        }

        // End date must be after start date
        if (request.getEndDate().isBefore(request.getStartDate()) || request.getEndDate().isEqual(request.getStartDate())) {
            throw new RuntimeException("End date must be after start date");
        }

        // Determine status based on current date and promotion dates using utility method
        String status = calculatePromotionStatus(currentDate, request.getStartDate(), request.getEndDate());

        // Create new promotion
        Promotion promotion = new Promotion();
        promotion.setPromotionName(request.getPromotionName());
        promotion.setDescription(request.getDescription());
        promotion.setValue(request.getValue());
        promotion.setType(request.getType());
        promotion.setStatus(status);
        promotion.setStartDate(request.getStartDate());
        promotion.setEndDate(request.getEndDate());
        promotion.setDealer(user.getDealer());

        // Save promotion
        Promotion savedPromotion = promotionRepository.save(promotion);

        return convertToResponse(savedPromotion);
    }

    /**
     * Tìm kiếm promotions theo status
     */
    public List<PromotionResponse> searchPromotionsByStatus(String email, String status) {
        // Find user by email
        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // Check if user is a dealer manager and has a dealer
        if (user.getDealer() == null) {
            throw new RuntimeException("User is not associated with any dealer");
        }

        // Validate status value
        if (status == null || status.trim().isEmpty()) {
            throw new RuntimeException("Status parameter is required");
        }

        // Normalize status for search (case insensitive)
        String normalizedStatus = status.trim();

        // Use existing repository method for status search
        List<Promotion> promotions = promotionRepository.findByDealerIdAndStatus(
                user.getDealer().getDealerId(), normalizedStatus);

        // Convert to response DTOs
        return promotions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Tìm kiếm promotions theo type
     */
    public List<PromotionResponse> searchPromotionsByType(String email, String type) {
        // Find user by email
        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // Check if user is a dealer manager and has a dealer
        if (user.getDealer() == null) {
            throw new RuntimeException("User is not associated with any dealer");
        }

        // Validate type value
        if (type == null || type.trim().isEmpty()) {
            throw new RuntimeException("Type parameter is required");
        }

        // Get all promotions for the dealer and filter by type
        List<Promotion> promotions = promotionRepository.findByDealerId(user.getDealer().getDealerId());

        // Filter promotions by type (case insensitive)
        String normalizedType = type.trim();
        List<Promotion> filteredPromotions = promotions.stream()
                .filter(promotion -> promotion.getType() != null &&
                        promotion.getType().equalsIgnoreCase(normalizedType))
                .collect(Collectors.toList());

        // Convert to response DTOs
        return filteredPromotions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Tìm kiếm promotions theo nhiều tiêu chí (type và status)
     */
    public List<PromotionResponse> searchPromotionsMultiCriteria(String email, String type, String status) {
        // Find user by email
        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // Check if user is a dealer manager and has a dealer
        if (user.getDealer() == null) {
            throw new RuntimeException("User is not associated with any dealer");
        }

        // Get all promotions for the dealer
        List<Promotion> promotions = promotionRepository.findByDealerId(user.getDealer().getDealerId());

        // Apply filters based on provided parameters
        List<Promotion> filteredPromotions = promotions.stream()
                .filter(promotion -> {
                    // Filter by type if provided
                    if (type != null && !type.trim().isEmpty()) {
                        if (promotion.getType() == null || !promotion.getType().equalsIgnoreCase(type.trim())) {
                            return false;
                        }
                    }
                    // Filter by status if provided
                    if (status != null && !status.trim().isEmpty()) {
                        if (promotion.getStatus() == null || !promotion.getStatus().equalsIgnoreCase(status.trim())) {
                            return false;
                        }
                    }

                    return true;
                })
                .collect(Collectors.toList());

        // Convert to response DTOs
        return filteredPromotions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert Promotion entity sang PromotionResponse DTO
     */
    private PromotionResponse convertToResponse(Promotion promotion) {
        return PromotionResponse.builder()
                .promotionId(promotion.getPromotionId())
                .promotionName(promotion.getPromotionName())
                .description(promotion.getDescription())
                .value(promotion.getValue())
                .type(promotion.getType())
                .status(promotion.getStatus())
                .startDate(promotion.getStartDate())
                .endDate(promotion.getEndDate())
                .dealerName(promotion.getDealer().getDealerName())
                .dealerId(promotion.getDealer().getDealerId())
                .build();
    }

    /**
     * Xóa promotion cho DealerManager
     */
    public String deletePromotionForDealerManager(Integer promotionId, String email) {
        // Find user by email
        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // Check if user is a dealer manager and has a dealer
        if (user.getDealer() == null) {
            throw new RuntimeException("User is not associated with any dealer");
        }

        // Find promotion by ID
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new RuntimeException("Promotion not found with ID: " + promotionId));

        // Check if promotion belongs to the user's dealer
        if (!promotion.getDealer().getDealerId().equals(user.getDealer().getDealerId())) {
            throw new RuntimeException("Access denied. This promotion does not belong to your dealer.");
        }

        promotionRepository.delete(promotion);
        return "Promotion with ID " + promotionId + " has been deleted successfully.";
    }
}
