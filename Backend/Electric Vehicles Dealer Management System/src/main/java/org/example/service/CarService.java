package org.example.service;

import org.example.dto.CarResponse;
import java.util.List;

public interface CarService {
    List<CarResponse> getAllCarsByCurrentDealer(String email);
    List<CarResponse> searchCarsByVariantOrModelName(String email, String searchTerm);
    List<CarResponse> searchCarsByPriceRange(String email, Double minPrice, Double maxPrice);

    // Methods for Admin/EVMStaff
    List<CarResponse> getAllCarsInSystem();
    List<CarResponse> searchCarsInSystem(String searchTerm);
    List<CarResponse> searchCarsByPriceRangeInSystem(Double minPrice, Double maxPrice);
}
