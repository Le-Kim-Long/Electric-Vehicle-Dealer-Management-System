package org.example.service;

import org.example.dto.CreatePromotionRequest;
import org.example.dto.PromotionResponse;
import org.example.dto.UpdatePromotionRequest;

import java.math.BigDecimal;
import java.util.List;

public interface PromotionService {

    /**
     * Lấy tất cả promotions của dealer mà user đang đăng nhập thuộc về
     */
    List<PromotionResponse> getPromotionsByDealerManager(String email);

    /**
     * Lấy thông tin promotion theo ID cho DealerManager
     */
    PromotionResponse getPromotionByIdForDealerManager(Integer promotionId, String email);

    /**
     * Cập nhật promotion cho DealerManager với logic xử lý ngày tháng
     */
    PromotionResponse updatePromotionForDealerManager(Integer promotionId, UpdatePromotionRequest request, String email);

    /**
     * Tạo promotion mới cho DealerManager
     */
    PromotionResponse createPromotionForDealerManager(CreatePromotionRequest request, String email);

    /**
     * Tìm kiếm promotions theo status
     */
    List<PromotionResponse> searchPromotionsByStatus(String email, String status);

    /**
     * Tìm kiếm promotions theo type
     */
    List<PromotionResponse> searchPromotionsByType(String email, String type);

    /**
     * Tìm kiếm promotions theo nhiều tiêu chí (type và status)
     */
    List<PromotionResponse> searchPromotionsMultiCriteria(String email, String type, String status);

    /**
     * Xóa promotion cho DealerManager
     */
    String deletePromotionForDealerManager(Integer promotionId, String email);
}
