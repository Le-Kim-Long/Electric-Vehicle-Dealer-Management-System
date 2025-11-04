package org.example.service;

import java.util.List;

public interface ColorService {
    List<String> getAllColorNames();

    List<String> getColorNamesByModelNameAndVariantName(String modelName, String variantName);
}
