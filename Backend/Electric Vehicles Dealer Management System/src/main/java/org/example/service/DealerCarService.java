package org.example.service;

import org.example.dto.DealerCarResponse;
import org.example.dto.UpdateDealerCarRequest;
import org.example.entity.DealerCar;
import org.example.repository.DealerCarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DealerCarService {

    @Autowired
    private DealerCarRepository dealerCarRepository;

    /**
     * Lấy tất cả xe của dealer (bao gồm cả pending và on sale) - cho dealer manager
     */
    public List<DealerCarResponse> getAllDealerCars(Integer dealerId) {
        List<DealerCar> dealerCars = dealerCarRepository.findDealerCarsByDealerId(dealerId);
        return convertToDealerCarResponse(dealerCars);
    }

    /**
     * Lấy chỉ xe có status "On Sale" - cho dealer staff
     */
    public List<DealerCarResponse> getOnSaleDealerCars(Integer dealerId) {
        List<DealerCar> dealerCars = dealerCarRepository.findOnSaleDealerCarsByDealerId(dealerId);
        return convertToDealerCarResponse(dealerCars);
    }

    /**
     * Cập nhật dealerPrice và status cho xe tại đại lý - cho dealer manager
     */
    public DealerCarResponse updateDealerCar(UpdateDealerCarRequest request) {
        Optional<DealerCar> dealerCarOpt = dealerCarRepository.findByCarIdAndDealerId(
                request.getCarId(), request.getDealerId());

        if (dealerCarOpt.isEmpty()) {
            throw new RuntimeException("Không tìm thấy xe tại đại lý này");
        }

        DealerCar dealerCar = dealerCarOpt.get();

        // Cập nhật dealerPrice nếu được cung cấp
        if (request.getDealerPrice() != null) {
            dealerCar.setDealerPrice(request.getDealerPrice());
        }

        // Cập nhật status nếu được cung cấp
        if (request.getStatus() != null) {
            dealerCar.setStatus(request.getStatus());
        }

        DealerCar savedDealerCar = dealerCarRepository.save(dealerCar);
        return convertToDealerCarResponse(List.of(savedDealerCar)).get(0);
    }

    /**
     * Khi thêm xe mới vào dealer, mặc định status = "Pending", dealerPrice = 0
     */
    public void addCarToDealer(Integer dealerId, Integer carId, Integer quantity) {
        // Kiểm tra xem xe đã tồn tại tại dealer chưa
        Optional<DealerCar> existingDealerCar = dealerCarRepository.findByCarIdAndDealerId(carId, dealerId);

        if (existingDealerCar.isPresent()) {
            // Nếu đã tồn tại, cộng thêm quantity
            DealerCar dealerCar = existingDealerCar.get();
            dealerCar.setQuantity(dealerCar.getQuantity() + quantity);
            dealerCarRepository.save(dealerCar);
        } else {
            // Nếu chưa tồn tại, tạo mới với status "Pending" và dealerPrice = 0
            DealerCar newDealerCar = new DealerCar();
            newDealerCar.setDealerId(dealerId);
            newDealerCar.setCarId(carId);
            newDealerCar.setQuantity(quantity);
            newDealerCar.setDealerPrice(BigDecimal.ZERO);
            newDealerCar.setStatus("Pending");
            dealerCarRepository.save(newDealerCar);
        }
    }

    private List<DealerCarResponse> convertToDealerCarResponse(List<DealerCar> dealerCars) {
        return dealerCars.stream()
                .map(dc -> {
                    return DealerCarResponse.builder()
                            .dealerId(dc.getDealerId())
                            .carId(dc.getCarId())
                            .quantity(dc.getQuantity())
                            .dealerPrice(dc.getDealerPrice())
                            .status(dc.getStatus())
                            .modelName(dc.getCar().getCarVariant().getCarModel().getModelName())
                            .variantName(dc.getCar().getCarVariant().getVariantName())
                            .colorName(dc.getCar().getColor() != null ? dc.getCar().getColor().getColorName() : null)
                            .productionYear(dc.getCar().getProductionYear())
                            .manufacturerPrice(dc.getCar().getPrice() != null ?
                                BigDecimal.valueOf(dc.getCar().getPrice()) : null) // Convert Long to BigDecimal
                            .imagePath(dc.getCar().getImagePath())
                            .build();
                })
                .collect(Collectors.toList());
    }
}
