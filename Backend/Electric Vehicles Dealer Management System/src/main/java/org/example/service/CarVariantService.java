package org.example.service;

import org.example.dto.DealerVariantDetailResponse;
import org.example.dto.VariantDetailResponse;

import java.util.List;

public interface CarVariantService {
    List<VariantDetailResponse> getVariantDetailsByCurrentDealer(String userEmail);
    List<VariantDetailResponse> searchVariantsByCurrentDealer(String userEmail, String searchTerm);

    // Methods for Admin/EVMStaff
    List<VariantDetailResponse> getAllVariantDetailsInSystem();
    List<VariantDetailResponse> searchVariantsInSystem(String searchTerm);

    // New method for getting variants by dealer name
    List<VariantDetailResponse> getVariantDetailsByDealerName(String dealerName);

    // New methods for searching by variant name specifically
    List<VariantDetailResponse> searchVariantsByVariantNameInSystem(String variantName);
    List<VariantDetailResponse> searchVariantsByVariantNameAndCurrentDealer(String userEmail, String variantName);

    // New methods for searching by model name specifically
    List<VariantDetailResponse> searchVariantsByModelNameInSystem(String modelName);
    List<VariantDetailResponse> searchVariantsByModelNameAndCurrentDealer(String userEmail, String modelName);

    // New methods for searching by both model name and variant name
    List<VariantDetailResponse> searchVariantsByModelAndVariantNameInSystem(String modelName, String variantName);
    List<VariantDetailResponse> searchVariantsByModelAndVariantNameAndCurrentDealer(String userEmail, String modelName, String variantName);

    // New methods for getting variant names and descriptions
    List<String> getAllVariantNames();
    String getDescriptionByVariantName(String variantName);
    String getDescriptionByModelNameAndVariantName(String modelName, String variantName);
    List<String> getVariantNamesByModelName(String modelName);

    // New methods for dealer staff and manager to get variant details with status
    List<DealerVariantDetailResponse> getDealerVariantDetailsForManager(Integer dealerId);
    List<DealerVariantDetailResponse> getDealerVariantDetailsForStaff(Integer dealerId);

    // Missing methods for dealer-specific searches
    List<DealerVariantDetailResponse> searchDealerVariantsForManager(Integer dealerId, String searchTerm);
    List<DealerVariantDetailResponse> searchDealerVariantsForStaff(Integer dealerId, String searchTerm);

    List<DealerVariantDetailResponse> searchDealerVariantsByVariantNameForManager(Integer dealerId, String variantName);
    List<DealerVariantDetailResponse> searchDealerVariantsByVariantNameForStaff(Integer dealerId, String variantName);

    List<DealerVariantDetailResponse> searchDealerVariantsByModelNameForManager(Integer dealerId, String modelName);
    List<DealerVariantDetailResponse> searchDealerVariantsByModelNameForStaff(Integer dealerId, String modelName);

    List<DealerVariantDetailResponse> searchDealerVariantsByModelAndVariantNameForManager(Integer dealerId, String modelName, String variantName);
    List<DealerVariantDetailResponse> searchDealerVariantsByModelAndVariantNameForStaff(Integer dealerId, String modelName, String variantName);

    // New method for searching by status - only for dealer manager
    List<DealerVariantDetailResponse> searchDealerVariantsByStatusForManager(Integer dealerId, String status);

    // New method for updating dealer car price and status - only for dealer manager
    boolean updateDealerCarPriceAndStatus(Integer dealerId, String modelName, String variantName,
                                         String colorName, java.math.BigDecimal dealerPrice, String status);
}
