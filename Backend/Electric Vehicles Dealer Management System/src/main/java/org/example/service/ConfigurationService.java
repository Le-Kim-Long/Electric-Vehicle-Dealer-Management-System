package org.example.service;

import org.example.dto.ConfigurationResponse;
import org.example.dto.UpdateConfigurationRequest;

public interface ConfigurationService {

    /**
     * Lấy configuration theo variant ID
     */
    ConfigurationResponse getConfigurationByVariantId(Integer variantId);

    /**
     * Lấy configuration theo variant name
     */
    ConfigurationResponse getConfigurationByVariantName(String variantName);

    /**
     * Lấy configuration theo model name và variant name
     */
    ConfigurationResponse getConfigurationByModelNameAndVariantName(String modelName, String variantName);

    /**
     * Cập nhật configuration theo model name và variant name
     */
    ConfigurationResponse updateConfigurationByModelNameAndVariantName(String modelName, String variantName, UpdateConfigurationRequest request);
}
