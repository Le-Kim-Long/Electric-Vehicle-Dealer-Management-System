package org.example.service.implementation;

import org.example.dto.CarResponse;
import org.example.entity.Car;
import org.example.entity.CarModel;
import org.example.entity.CarVariant;
import org.example.entity.Configuration;
import org.example.entity.DealerCar;
import org.example.entity.UserAccount;
import org.example.repository.DealerCarRepository;
import org.example.repository.UserAccountRepository;
import org.example.service.CarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Override
    public List<CarResponse> getAllCarsByCurrentDealer(String email) {
        // Tìm user account với dealer thông tin theo email
        UserAccount userAccount = userAccountRepository.findByEmailWithDealer(email)
                .orElseThrow(() -> new RuntimeException("User not found or not associated with any dealer"));

        if (userAccount.getDealer() == null) {
            throw new RuntimeException("User is not associated with any dealer");
        }

        // Lấy tất cả xe của dealer
        List<DealerCar> dealerCars = dealerCarRepository.findDealerCarsByDealerId(userAccount.getDealer().getDealerId());

        // Convert sang DTO
        return dealerCars.stream()
                .map(this::convertToCarResponse)
                .toList();
    }

    @Override
    public List<CarResponse> searchCarsByVariantOrModelName(String email, String searchTerm) {
        // Tìm user account với dealer thông tin theo email
        UserAccount userAccount = userAccountRepository.findByEmailWithDealer(email)
                .orElseThrow(() -> new RuntimeException("User not found or not associated with any dealer"));

        if (userAccount.getDealer() == null) {
            throw new RuntimeException("User is not associated with any dealer");
        }

        // Lấy tất cả xe của dealer trước
        List<DealerCar> dealerCars = dealerCarRepository.findDealerCarsByDealerId(userAccount.getDealer().getDealerId());

        // Xử lý tìm kiếm linh động trên danh sách DealerCar
        List<DealerCar> filteredDealerCars = performFlexibleSearchOnDealerCars(dealerCars, searchTerm);

        // Convert sang DTO
        return filteredDealerCars.stream()
                .map(this::convertToCarResponse)
                .toList();
    }

    @Override
    public List<CarResponse> searchCarsByPriceRange(String email, Double minPrice, Double maxPrice) {
        // Tìm user account với dealer thông tin theo email
        UserAccount userAccount = userAccountRepository.findByEmailWithDealer(email)
                .orElseThrow(() -> new RuntimeException("User not found or not associated with any dealer"));

        if (userAccount.getDealer() == null) {
            throw new RuntimeException("User is not associated with any dealer");
        }

        // Validate price range
        if (minPrice < 0 || maxPrice < 0) {
            throw new RuntimeException("Price values cannot be negative");
        }

        if (minPrice > maxPrice) {
            throw new RuntimeException("Minimum price cannot be greater than maximum price");
        }

        // Lấy tất cả xe của dealer trước
        List<DealerCar> dealerCars = dealerCarRepository.findDealerCarsByDealerId(userAccount.getDealer().getDealerId());

        // Lọc theo khoảng giá
        List<DealerCar> filteredDealerCars = dealerCars.stream()
                .filter(dealerCar -> {
                    Long carPrice = dealerCar.getCar().getPrice();
                    return carPrice != null && carPrice >= minPrice && carPrice <= maxPrice;
                })
                .toList();

        // Convert sang DTO
        return filteredDealerCars.stream()
                .map(this::convertToCarResponse)
                .toList(); // Updated to use toList()
    }

    // Methods for Admin/EVMStaff to view all cars in system
    @Override
    public List<CarResponse> getAllCarsInSystem() {
        // Lấy tất cả xe trong hệ thống (không giới hạn theo dealer)
        List<DealerCar> allDealerCars = dealerCarRepository.findAll();

        return allDealerCars.stream()
                .map(this::convertToCarResponse)
                .toList();
    }

    @Override
    public List<CarResponse> searchCarsInSystem(String searchTerm) {
        // Lấy tất cả xe trong hệ thống
        List<DealerCar> allDealerCars = dealerCarRepository.findAll();

        // Thực hiện tìm kiếm linh động trên toàn bộ danh sách
        List<DealerCar> filteredDealerCars = performFlexibleSearchOnDealerCars(allDealerCars, searchTerm);

        return filteredDealerCars.stream()
                .map(this::convertToCarResponse)
                .toList();
    }

    @Override
    public List<CarResponse> searchCarsByPriceRangeInSystem(Double minPrice, Double maxPrice) {
        // Validate price range
        if (minPrice < 0 || maxPrice < 0) {
            throw new RuntimeException("Price values cannot be negative");
        }

        if (minPrice > maxPrice) {
            throw new RuntimeException("Minimum price cannot be greater than maximum price");
        }

        // Lấy tất cả xe trong hệ thống
        List<DealerCar> allDealerCars = dealerCarRepository.findAll();

        // Lọc theo khoảng giá
        List<DealerCar> filteredDealerCars = allDealerCars.stream()
                .filter(dealerCar -> {
                    Long carPrice = dealerCar.getCar().getPrice();
                    return carPrice != null && carPrice >= minPrice && carPrice <= maxPrice;
                })
                .toList();

        return filteredDealerCars.stream()
                .map(this::convertToCarResponse)
                .toList();
    }

    private List<DealerCar> performFlexibleSearchOnDealerCars(List<DealerCar> dealerCars, String searchTerm) {
        // Chuẩn hóa search term
        String normalizedSearchTerm = normalizeSearchTerm(searchTerm);

        // Tách searchTerm thành các từ khóa riêng biệt
        String[] keywords = normalizedSearchTerm.toLowerCase().trim().split("\\s+");

        // Trước tiên thử tìm kiếm với toàn bộ chuỗi gốc
        List<DealerCar> exactResults = searchDealerCarsByTerm(dealerCars, searchTerm);

        if (!exactResults.isEmpty()) {
            return exactResults;
        }

        // Thử tìm kiếm với chuỗi đã chuẩn hóa
        if (!normalizedSearchTerm.equals(searchTerm)) {
            List<DealerCar> normalizedResults = searchDealerCarsByTerm(dealerCars, normalizedSearchTerm);
            if (!normalizedResults.isEmpty()) {
                return normalizedResults;
            }
        }

        // Tìm kiếm với substring (không có khoảng trắng)
        List<DealerCar> substringResults = searchDealerCarsBySubstrings(dealerCars, searchTerm.toLowerCase());
        if (!substringResults.isEmpty()) {
            return filterDealerCarsByKeywordRelevance(substringResults, keywords, searchTerm.toLowerCase());
        }

        // Nếu vẫn không tìm thấy, thử tìm kiếm với từng từ khóa riêng biệt
        Set<DealerCar> combinedResults = new LinkedHashSet<>();

        for (String keyword : keywords) {
            if (keyword.length() >= 2) { // Chỉ tìm kiếm từ khóa có ít nhất 2 ký tự
                List<DealerCar> keywordResults = searchDealerCarsByTerm(dealerCars, keyword);
                combinedResults.addAll(keywordResults);
            }
        }

        // Lọc kết quả để chỉ giữ lại những xe có chứa nhiều từ khóa nhất
        return filterDealerCarsByKeywordRelevance(new ArrayList<>(combinedResults), keywords, searchTerm.toLowerCase());
    }

    private List<DealerCar> searchDealerCarsByTerm(List<DealerCar> dealerCars, String searchTerm) {
        return dealerCars.stream()
                .filter(dealerCar -> {
                    Car car = dealerCar.getCar();
                    String modelName = car.getCarVariant().getCarModel().getModelName().toLowerCase();
                    String variantName = car.getCarVariant().getVariantName().toLowerCase();

                    return modelName.contains(searchTerm.toLowerCase()) ||
                           variantName.contains(searchTerm.toLowerCase());
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

            // Kiểm tra substring match - chỉ thêm nếu có match thực sự
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
        // Tính điểm relevance cho mỗi xe
        return dealerCars.stream()
                .map(dealerCar -> new DealerCarWithScore(dealerCar, calculateDealerCarRelevanceScore(dealerCar, keywords, originalSearchTerm)))
                .filter(dealerCarWithScore -> dealerCarWithScore.score > 0)
                .sorted((a, b) -> Integer.compare(b.score, a.score)) // Sắp xếp theo điểm giảm dần
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

        // Kiểm tra match chính xác với search term gốc
        if (fullNameNoSpace.contains(originalSearchTerm) ||
                fullName.contains(originalSearchTerm) ||
                modelName.replaceAll("\\s+", "").contains(originalSearchTerm) ||
                variantName.replaceAll("\\s+", "").contains(originalSearchTerm)) {
            score += 10; // Điểm cao cho match chính xác
        }

        // Kiểm tra match với từng keyword
        for (String keyword : keywords) {
            String lowerKeyword = keyword.toLowerCase();
            if (modelName.contains(lowerKeyword) || variantName.contains(lowerKeyword)) {
                score += 2;
            }
            // Thưởng điểm nếu tìm thấy từ khóa trong tên đầy đủ
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

        // Chuyển đổi power từ Double sang Integer
        Integer powerValue = null;
        if (config != null && config.getPower() != null) {
            powerValue = config.getPower().intValue();
        }

        // Xây dựng URL để truy cập ảnh qua HTTP
        String imageUrl = null;
        if (car.getImagePath() != null && !car.getImagePath().isEmpty()) {
            imageUrl = "/api/images/" + car.getImagePath();
        }

        return CarResponse.builder()
                .carId(car.getCarId())
                .modelName(model.getModelName())
                .segment(model.getSegment())
                .variantName(variant.getVariantName())
                .color(car.getColor() != null ? car.getColor().getColor_name() : null)
                .price(car.getPrice())
                .rangeKm(config != null ? config.getRangeKm() : null)
                .power(powerValue)
                .quantity(dealerCar.getQuantity())
                .imagePath(imageUrl)
                .build();
    }

    // Inner class để lưu trữ dealerCar với điểm số
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

        // Thêm khoảng trắng giữa chữ và số
        // Ví dụ: "vf3eco" -> "vf3 eco", "vf8plus" -> "vf8 plus"
        normalized = normalized.replaceAll("([a-zA-Z]+)(\\d+)", "$1 $2"); // chữ + số
        normalized = normalized.replaceAll("(\\d+)([a-zA-Z]+)", "$1 $2"); // số + chữ

        // Xử lý các từ phổ biến trong tên xe
        normalized = normalized.replaceAll("(vf\\d+)(eco|plus|standard|premium|luxury)", "$1 $2");
        normalized = normalized.replaceAll("(vinfast)(\\w+)", "$1 $2");

        // Loại bỏ khoảng trắng thừa
        normalized = normalized.replaceAll("\\s+", " ").trim();

        return normalized;
    }
}
