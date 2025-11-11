package org.example.service.implementation;

import org.example.dto.AddCarToDealerRequest;
import org.example.dto.CarResponse;
import org.example.dto.CreateCarRequest;
import org.example.dto.CreateCompleteCarRequest;
import org.example.dto.UpdateManufacturerPriceRequest;
import org.example.entity.Car;
import org.example.entity.CarModel;
import org.example.entity.CarVariant;
import org.example.entity.Color;
import org.example.entity.Configuration;
import org.example.entity.Dealer;
import org.example.entity.DealerCar;
import org.example.entity.UserAccount;
import org.example.repository.CarModelRepository;
import org.example.repository.CarRepository;
import org.example.repository.CarVariantRepository;
import org.example.repository.ColorRepository;
import org.example.repository.ConfigurationRepository;
import org.example.repository.DealerCarRepository;
import org.example.repository.DealerRepository;
import org.example.repository.UserAccountRepository;
import org.example.service.CarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class CarServiceImpl implements CarService {

    @Autowired
    private DealerCarRepository dealerCarRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private CarVariantRepository carVariantRepository;

    @Autowired
    private ColorRepository colorRepository;

    @Autowired
    private CarModelRepository carModelRepository;

    @Autowired
    private ConfigurationRepository configurationRepository;

    @Autowired
    private DealerRepository dealerRepository;

    @Override
    public List<CarResponse> getAllCarsByCurrentDealer(String email) {
        UserAccount userAccount = userAccountRepository.findByEmailWithDealer(email)
                .orElseThrow(() -> new RuntimeException("User not found or not associated with any dealer"));

        if (userAccount.getDealer() == null) {
            throw new RuntimeException("User is not associated with any dealer");
        }

        List<DealerCar> dealerCars = dealerCarRepository.findDealerCarsByDealerId(userAccount.getDealer().getDealerId());

        return dealerCars.stream()
                .map(this::convertToCarResponse)
                .toList();
    }

    @Override
    public List<CarResponse> searchCarsByVariantOrModelName(String email, String searchTerm) {
        UserAccount userAccount = userAccountRepository.findByEmailWithDealer(email)
                .orElseThrow(() -> new RuntimeException("User not found or not associated with any dealer"));

        if (userAccount.getDealer() == null) {
            throw new RuntimeException("User is not associated with any dealer");
        }

        List<DealerCar> dealerCars = dealerCarRepository.findDealerCarsByDealerId(userAccount.getDealer().getDealerId());
        List<DealerCar> filteredDealerCars = performFlexibleSearchOnDealerCars(dealerCars, searchTerm);

        return filteredDealerCars.stream()
                .map(this::convertToCarResponse)
                .toList();
    }

    @Override
    public List<CarResponse> getAllCarsInSystem() {
        List<DealerCar> allDealerCars = dealerCarRepository.findAll();

        return allDealerCars.stream()
                .map(this::convertToCarResponse)
                .toList();
    }

    @Override
    public List<CarResponse> searchCarsInSystem(String searchTerm) {
        List<DealerCar> allDealerCars = dealerCarRepository.findAll();
        List<DealerCar> filteredDealerCars = performFlexibleSearchOnDealerCars(allDealerCars, searchTerm);

        return filteredDealerCars.stream()
                .map(this::convertToCarResponse)
                .toList();
    }

    @Override
    @Transactional
    public CarResponse addCarToSystem(CreateCarRequest request) {
        CarVariant carVariant = carVariantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new RuntimeException("Car variant not found with ID: " + request.getVariantId()));

        Color color = colorRepository.findById(request.getColorId())
                .orElseThrow(() -> new RuntimeException("Color not found with ID: " + request.getColorId()));

        Car car = new Car();
        car.setVariantId(request.getVariantId());
        car.setColorId(request.getColorId());
        car.setProductionYear(request.getProductionYear());
        car.setPrice(request.getPrice());
        car.setImagePath(request.getImagePath());

        Car savedCar = carRepository.save(car);

        CarModel model = carVariant.getCarModel();
        Configuration config = carVariant.getConfiguration();

        Integer powerValue = null;
        if (config != null && config.getPower() != null) {
            powerValue = config.getPower().intValue();
        }

        String imageUrl = null;
        if (savedCar.getImagePath() != null && !savedCar.getImagePath().isEmpty()) {
            imageUrl = "/api/images/" + savedCar.getImagePath();
        }

        return CarResponse.builder()
                .carId(savedCar.getCarId())
                .modelName(model.getModelName())
                .segment(model.getSegment())
                .variantName(carVariant.getVariantName())
                .color(color.getColorName())
                .price(savedCar.getPrice())
                .rangeKm(config != null ? config.getRangeKm() : null)
                .power(powerValue)
                .quantity(null)
                .imagePath(imageUrl)
                .build();
    }

    @Override
    @Transactional
    public CarResponse addCompleteCarToSystem(CreateCompleteCarRequest request) {
        // Step 1: Find or create CarModel
        CarModel carModel = carModelRepository.findByModelNameIgnoreCase(request.getModel().getModelName())
                .orElseGet(() -> {
                    CarModel newModel = new CarModel();
                    newModel.setModelName(request.getModel().getModelName());
                    newModel.setSegment(request.getModel().getSegment());
                    return carModelRepository.save(newModel);
                });

        // Step 2: Find or create CarVariant
        CarVariant carVariant = carVariantRepository.findByVariantNameIgnoreCaseAndModelId(
                request.getVariant().getVariantName(),
                carModel.getModelId())
                .orElseGet(() -> {
                    CarVariant newVariant = new CarVariant();
                    newVariant.setVariantName(request.getVariant().getVariantName());
                    newVariant.setModelId(carModel.getModelId());
                    newVariant.setDescription(request.getVariant().getDescription());
                    return carVariantRepository.save(newVariant);
                });

        // Step 3: Find or create Configuration
        Configuration configuration = configurationRepository.findByVariantId(carVariant.getVariantId())
                .orElseGet(() -> {
                    Configuration newConfig = new Configuration();
                    newConfig.setVariantId(carVariant.getVariantId());
                    newConfig.setBatteryCapacity(request.getConfiguration().getBatteryCapacity());
                    newConfig.setBatteryType(request.getConfiguration().getBatteryType());
                    newConfig.setFullChargeTime(request.getConfiguration().getFullChargeTime());
                    newConfig.setRangeKm(request.getConfiguration().getRangeKm());
                    newConfig.setPower(request.getConfiguration().getPower());
                    newConfig.setTorque(request.getConfiguration().getTorque());
                    newConfig.setLengthMm(request.getConfiguration().getLengthMm());
                    newConfig.setWidthMm(request.getConfiguration().getWidthMm());
                    newConfig.setHeightMm(request.getConfiguration().getHeightMm());
                    newConfig.setWheelbaseMm(request.getConfiguration().getWheelbaseMm());
                    newConfig.setWeightKg(request.getConfiguration().getWeightKg());
                    newConfig.setTrunkVolumeL(request.getConfiguration().getTrunkVolumeL());
                    newConfig.setSeats(request.getConfiguration().getSeats());
                    return configurationRepository.save(newConfig);
                });

        // Step 4: Find or create Color
        Color color = colorRepository.findByColorNameIgnoreCase(request.getColor())
                .orElseGet(() -> {
                    Color newColor = new Color();
                    newColor.setColorName(request.getColor());
                    return colorRepository.save(newColor);
                });

        // Step 5: Check for duplicate car before creating
        boolean isDuplicate = carRepository.existsDuplicateCar(
                carVariant.getVariantId(),
                color.getColorId()
        );

        if (isDuplicate) {
            throw new RuntimeException(
                "Car with same variant and color already exists: " +
                "Model=" + carModel.getModelName() +
                ", Variant=" + carVariant.getVariantName() +
                ", Color=" + color.getColorName()
            );
        }

        // Step 6: Create Car (only if not duplicate)
        Car car = new Car();
        car.setVariantId(carVariant.getVariantId());
        car.setColorId(color.getColorId());
        car.setProductionYear(request.getCar().getProductionYear());
        car.setPrice(request.getCar().getPrice()); // Fix: Add missing price assignment
        car.setImagePath(request.getCar().getImagePath());

        Car savedCar = carRepository.save(car);

        // Step 7: Build and return response
        Integer powerValue = null;
        if (configuration.getPower() != null) {
            powerValue = configuration.getPower().intValue();
        }

        String imageUrl = null;
        if (savedCar.getImagePath() != null && !savedCar.getImagePath().isEmpty()) {
            imageUrl = "/api/images/" + savedCar.getImagePath();
        }

        return CarResponse.builder()
                .carId(savedCar.getCarId())
                .modelName(carModel.getModelName())
                .segment(carModel.getSegment())
                .variantName(carVariant.getVariantName())
                .color(color.getColorName())
                .price(savedCar.getPrice())
                .rangeKm(configuration.getRangeKm())
                .power(powerValue)
                .quantity(null)
                .imagePath(imageUrl)
                .build();
    }

    @Override
    @Transactional
    public String addCarToDealer(AddCarToDealerRequest request) {
        // Validate input
        if (request.getModelName() == null || request.getVariantName() == null ||
                request.getColorName() == null || request.getDealerName() == null || request.getQuantity() == null) {
            throw new RuntimeException("All fields are required: modelName, variantName, colorName, dealerName, quantity");
        }

        if (request.getQuantity() <= 0) {
            throw new RuntimeException("Quantity must be greater than 0");
        }

        // Find dealer by name
        Dealer dealer = dealerRepository.findByDealerName(request.getDealerName())
                .orElseThrow(() -> new RuntimeException("Dealer not found: " + request.getDealerName()));

        // Find car by modelName, variantName and colorName
        Car car = carRepository.findByModelNameAndVariantNameAndColorName(
                request.getModelName(), request.getVariantName(), request.getColorName())
                .orElseThrow(() -> new RuntimeException(
                        "Car not found with model: " + request.getModelName() +
                                ", variant: " + request.getVariantName() +
                                " and color: " + request.getColorName()));

        // Check if dealer already has this car
        DealerCar existingDealerCar = dealerCarRepository.findByCarIdAndDealerId(
                car.getCarId(), dealer.getDealerId()).orElse(null);

        if (existingDealerCar != null) {
            // Dealer already has this car - add to quantity, keep existing dealer price and status
            int newQuantity = existingDealerCar.getQuantity() + request.getQuantity();
            existingDealerCar.setQuantity(newQuantity);
            dealerCarRepository.save(existingDealerCar);

            return String.format("Successfully added %d cars to dealer %s. Total quantity now: %d. " +
                            "Model: %s %s (%s). Dealer price: %s, Status: %s (unchanged)",
                    request.getQuantity(), request.getDealerName(), newQuantity,
                    request.getModelName(), request.getVariantName(), request.getColorName(),
                    existingDealerCar.getDealerPrice() != null ? existingDealerCar.getDealerPrice() : "0",
                    existingDealerCar.getStatus() != null ? existingDealerCar.getStatus() : "Pending");
        } else {
            // Dealer doesn't have this car - create new entry with dealer price = 0 and status = "Pending"
            DealerCar newDealerCar = new DealerCar();
            newDealerCar.setCarId(car.getCarId());
            newDealerCar.setDealerId(dealer.getDealerId());
            newDealerCar.setQuantity(request.getQuantity());
            newDealerCar.setDealerPrice(BigDecimal.ZERO); // Set dealer price to 0
            newDealerCar.setStatus("Pending"); // Set status to "Pending"
            dealerCarRepository.save(newDealerCar);

            return String.format("Successfully added %d cars to dealer %s. " +
                            "Model: %s %s (%s). New car added with dealer price: 0, status: Pending",
                    request.getQuantity(), request.getDealerName(),
                    request.getModelName(), request.getVariantName(), request.getColorName());
        }
    }

    @Override
    @Transactional
    public String updateManufacturerPrice(String modelName, String variantName, String colorName, UpdateManufacturerPriceRequest request) {
        // Validate input parameters
        if (modelName == null || modelName.trim().isEmpty()) {
            throw new RuntimeException("Model name is required and cannot be empty");
        }
        if (variantName == null || variantName.trim().isEmpty()) {
            throw new RuntimeException("Variant name is required and cannot be empty");
        }
        if (colorName == null || colorName.trim().isEmpty()) {
            throw new RuntimeException("Color name is required and cannot be empty");
        }
        if (request == null || request.getManufacturerPrice() == null) {
            throw new RuntimeException("Manufacturer price is required");
        }

        // Find car by model name, variant name, and color name
        Car car = carRepository.findByModelNameAndVariantNameAndColorName(
                modelName.trim(), variantName.trim(), colorName.trim())
                .orElseThrow(() -> new RuntimeException(
                        "Car not found with model: " + modelName.trim() +
                                ", variant: " + variantName.trim() +
                                ", color: " + colorName.trim()));

        // Store old price for response message
        Long oldPrice = car.getPrice();

        // Update manufacturer price
        car.setPrice(request.getManufacturerPrice());
        carRepository.save(car);

        return String.format("Successfully updated manufacturer price for car: %s %s (%s). " +
                        "Previous price: %s VND, New price: %s VND",
                modelName.trim(), variantName.trim(), colorName.trim(),
                oldPrice != null ? String.format("%,d", oldPrice) : "Not set",
                String.format("%,d", request.getManufacturerPrice()));
    }

    @Override
    public Long getManufacturerPrice(String modelName, String variantName, String colorName) {
        // Validate input parameters
        if (modelName == null || modelName.trim().isEmpty()) {
            throw new RuntimeException("Model name is required and cannot be empty");
        }
        if (variantName == null || variantName.trim().isEmpty()) {
            throw new RuntimeException("Variant name is required and cannot be empty");
        }
        if (colorName == null || colorName.trim().isEmpty()) {
            throw new RuntimeException("Color name is required and cannot be empty");
        }

        // Find car by model name, variant name, and color name
        Car car = carRepository.findByModelNameAndVariantNameAndColorName(
                modelName.trim(), variantName.trim(), colorName.trim())
                .orElseThrow(() -> new RuntimeException(
                        "Car not found with model: " + modelName.trim() +
                                ", variant: " + variantName.trim() +
                                ", color: " + colorName.trim()));

        return car.getPrice();
    }

    private List<DealerCar> performFlexibleSearchOnDealerCars(List<DealerCar> dealerCars, String searchTerm) {
        String normalizedSearchTerm = normalizeSearchTerm(searchTerm);
        String[] keywords = normalizedSearchTerm.toLowerCase().trim().split("\\s+");

        List<DealerCar> exactResults = searchDealerCarsByTerm(dealerCars, searchTerm);
        if (!exactResults.isEmpty()) {
            return exactResults;
        }

        if (!normalizedSearchTerm.equals(searchTerm)) {
            List<DealerCar> normalizedResults = searchDealerCarsByTerm(dealerCars, normalizedSearchTerm);
            if (!normalizedResults.isEmpty()) {
                return normalizedResults;
            }
        }

        List<DealerCar> substringResults = searchDealerCarsBySubstrings(dealerCars, searchTerm.toLowerCase());
        if (!substringResults.isEmpty()) {
            return filterDealerCarsByKeywordRelevance(substringResults, keywords, searchTerm.toLowerCase());
        }

        Set<DealerCar> combinedResults = new LinkedHashSet<>();
        for (String keyword : keywords) {
            if (keyword.length() >= 2) {
                List<DealerCar> keywordResults = searchDealerCarsByTerm(dealerCars, keyword);
                combinedResults.addAll(keywordResults);
            }
        }

        return filterDealerCarsByKeywordRelevance(new ArrayList<>(combinedResults), keywords, searchTerm.toLowerCase());
    }

    private List<DealerCar> searchDealerCarsByTerm(List<DealerCar> dealerCars, String searchTerm) {
        return dealerCars.stream()
                .filter(dealerCar -> {
                    Car car = dealerCar.getCar();
                    String modelName = car.getCarVariant().getCarModel().getModelName().toLowerCase();
                    String variantName = car.getCarVariant().getVariantName().toLowerCase();
                    return modelName.contains(searchTerm.toLowerCase()) || variantName.contains(searchTerm.toLowerCase());
                })
                .toList();
    }

    private List<DealerCar> searchDealerCarsBySubstrings(List<DealerCar> dealerCars, String searchTerm) {
        List<DealerCar> matchingDealerCars = new ArrayList<>();

        for (DealerCar dealerCar : dealerCars) {
            Car car = dealerCar.getCar();
            String modelName = car.getCarVariant().getCarModel().getModelName().toLowerCase();
            String variantName = car.getCarVariant().getVariantName().toLowerCase();
            String fullName = (modelName + variantName).replaceAll("\\s+", "");
            String fullNameWithSpace = (modelName + " " + variantName).toLowerCase();

            boolean hasMatch = false;
            if (fullName.contains(searchTerm) && searchTerm.length() >= 3) {
                hasMatch = true;
            } else if (fullNameWithSpace.contains(searchTerm) && searchTerm.length() >= 3) {
                hasMatch = true;
            } else if (modelName.replaceAll("\\s+", "").contains(searchTerm) && searchTerm.length() >= 2) {
                hasMatch = true;
            } else if (variantName.replaceAll("\\s+", "").contains(searchTerm) && searchTerm.length() >= 2) {
                hasMatch = true;
            }

            if (hasMatch) {
                matchingDealerCars.add(dealerCar);
            }
        }

        return matchingDealerCars;
    }

    private List<DealerCar> filterDealerCarsByKeywordRelevance(List<DealerCar> dealerCars, String[] keywords, String originalSearchTerm) {
        return dealerCars.stream()
                .map(dealerCar -> new DealerCarWithScore(dealerCar, calculateDealerCarRelevanceScore(dealerCar, keywords, originalSearchTerm)))
                .filter(dealerCarWithScore -> dealerCarWithScore.score > 0)
                .sorted((a, b) -> Integer.compare(b.score, a.score))
                .map(dealerCarWithScore -> dealerCarWithScore.dealerCar)
                .toList();
    }

    private int calculateDealerCarRelevanceScore(DealerCar dealerCar, String[] keywords, String originalSearchTerm) {
        int score = 0;
        Car car = dealerCar.getCar();
        String modelName = car.getCarVariant().getCarModel().getModelName().toLowerCase();
        String variantName = car.getCarVariant().getVariantName().toLowerCase();
        String fullName = (modelName + " " + variantName).toLowerCase();
        String fullNameNoSpace = (modelName + variantName).replaceAll("\\s+", "");

        if (fullNameNoSpace.contains(originalSearchTerm) ||
                fullName.contains(originalSearchTerm) ||
                modelName.replaceAll("\\s+", "").contains(originalSearchTerm) ||
                variantName.replaceAll("\\s+", "").contains(originalSearchTerm)) {
            score += 10;
        }

        for (String keyword : keywords) {
            String lowerKeyword = keyword.toLowerCase();
            if (modelName.contains(lowerKeyword) || variantName.contains(lowerKeyword)) {
                score += 2;
            }
            if (fullName.contains(lowerKeyword)) {
                score++;
            }
        }

        return score;
    }

    private CarResponse convertToCarResponse(DealerCar dealerCar) {
        Car car = dealerCar.getCar();
        CarVariant variant = car.getCarVariant();
        CarModel model = variant.getCarModel();
        Configuration config = variant.getConfiguration();

        Integer powerValue = null;
        if (config != null && config.getPower() != null) {
            powerValue = config.getPower().intValue();
        }

        String imageUrl = null;
        if (car.getImagePath() != null && !car.getImagePath().isEmpty()) {
            imageUrl = "/api/images/" + car.getImagePath();
        }

        return CarResponse.builder()
                .carId(car.getCarId())
                .modelName(model.getModelName())
                .segment(model.getSegment())
                .variantName(variant.getVariantName())
                .color(car.getColor() != null ? car.getColor().getColorName() : null)
                .price(car.getPrice())
                .rangeKm(config != null ? config.getRangeKm() : null)
                .power(powerValue)
                .quantity(dealerCar.getQuantity())
                .imagePath(imageUrl)
                .build();
    }

    private static class DealerCarWithScore {
        final DealerCar dealerCar;
        final int score;

        DealerCarWithScore(DealerCar dealerCar, int score) {
            this.dealerCar = dealerCar;
            this.score = score;
        }
    }

    private String normalizeSearchTerm(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return searchTerm;
        }

        String normalized = searchTerm.toLowerCase().trim();
        normalized = normalized.replaceAll("([a-zA-Z]+)(\\d+)", "$1 $2");
        normalized = normalized.replaceAll("(\\d+)([a-zA-Z]+)", "$1 $2");
        normalized = normalized.replaceAll("(vf\\d+)(eco|plus|standard|premium|luxury)", "$1 $2");
        normalized = normalized.replaceAll("(vinfast)(\\w+)", "$1 $2");
        normalized = normalized.replaceAll("\\s+", " ").trim();

        return normalized;
    }
}
