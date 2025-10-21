package org.example.service;

import org.example.dto.CarResponse;
import org.example.dto.CreateCarRequest;
import org.example.dto.CreateCompleteCarRequest;
import java.util.List;

public interface CarService {
    List<CarResponse> getAllCarsByCurrentDealer(String email);
    List<CarResponse> searchCarsByVariantOrModelName(String email, String searchTerm);

    // Methods for Admin/EVMStaff
    List<CarResponse> getAllCarsInSystem();
    List<CarResponse> searchCarsInSystem(String searchTerm);

    // Method to add new car to system (Admin/EVMStaff only)
    CarResponse addCarToSystem(CreateCarRequest request);

    // Method to add complete car with all info (Admin/EVMStaff only)
    CarResponse addCompleteCarToSystem(CreateCompleteCarRequest request);
}
