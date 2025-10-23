package org.example.service;

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
}
