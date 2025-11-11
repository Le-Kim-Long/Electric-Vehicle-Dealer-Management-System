package org.example.service.implementation;

import lombok.RequiredArgsConstructor;
import org.example.repository.CarModelRepository;
import org.example.service.CarModelService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CarModelServiceImpl implements CarModelService {

    private final CarModelRepository carModelRepository;

    @Override
    public List<String> getAllModelNames() {
        return carModelRepository.findAllModelNames();
    }

    @Override
    public String getSegmentByModelName(String modelName) {
        return carModelRepository.findSegmentByModelName(modelName)
                .orElse(null);
    }
}
