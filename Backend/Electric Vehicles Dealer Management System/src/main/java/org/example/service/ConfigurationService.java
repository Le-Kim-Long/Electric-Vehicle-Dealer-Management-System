package org.example.service;

import org.example.dto.ConfigurationResponse;

public interface ConfigurationService {

    /**
     * Lấy configuration theo variant ID
     */
    ConfigurationResponse getConfigurationByVariantId(Integer variantId);
}

