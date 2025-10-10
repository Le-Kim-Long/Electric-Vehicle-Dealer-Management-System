package org.example.service;

import org.example.dto.VariantDetailResponse;

import java.util.List;

public interface CarVariantService {
    List<VariantDetailResponse> getVariantDetailsByCurrentDealer(String userEmail);
    List<VariantDetailResponse> searchVariantsByCurrentDealer(String userEmail, String searchTerm);

    // Methods for Admin/EVMStaff
    List<VariantDetailResponse> getAllVariantDetailsInSystem();
    List<VariantDetailResponse> searchVariantsInSystem(String searchTerm);
}
