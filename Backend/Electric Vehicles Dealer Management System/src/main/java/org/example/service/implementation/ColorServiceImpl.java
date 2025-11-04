package org.example.service.implementation;

import lombok.RequiredArgsConstructor;
import org.example.repository.ColorRepository;
import org.example.service.ColorService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ColorServiceImpl implements ColorService {

    private final ColorRepository colorRepository;

    @Override
    public List<String> getAllColorNames() {
        return colorRepository.findAllColorNames();
    }

    @Override
    public List<String> getColorNamesByModelNameAndVariantName(String modelName, String variantName) {
        if (modelName == null || modelName.trim().isEmpty()) {
            throw new RuntimeException("Model name is required and cannot be empty");
        }
        if (variantName == null || variantName.trim().isEmpty()) {
            throw new RuntimeException("Variant name is required and cannot be empty");
        }

        return colorRepository.findColorNamesByModelNameAndVariantName(modelName.trim(), variantName.trim());
    }
}
