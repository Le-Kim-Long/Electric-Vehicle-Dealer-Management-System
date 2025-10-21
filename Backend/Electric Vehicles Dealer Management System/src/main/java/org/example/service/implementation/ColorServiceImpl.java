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
}
