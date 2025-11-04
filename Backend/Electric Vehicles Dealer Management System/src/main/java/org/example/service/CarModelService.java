package org.example.service;

import java.util.List;

public interface CarModelService {
    List<String> getAllModelNames();
    String getSegmentByModelName(String modelName);
}
