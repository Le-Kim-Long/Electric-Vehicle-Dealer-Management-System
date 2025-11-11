package org.example.service.implementation;

import org.example.dto.CreateDistributionRequestDto;
import org.example.dto.DistributionRequestResponseDto;
import org.example.entity.Car;
import org.example.entity.CarDistributionRequest;
import org.example.entity.Dealer;
import org.example.entity.DealerCar;
import org.example.entity.UserAccount;
import org.example.repository.CarDistributionRequestRepository;
import org.example.repository.CarRepository;
import org.example.repository.DealerCarRepository;
import org.example.repository.UserAccountRepository;
import org.example.service.CarDistributionRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CarDistributionRequestServiceImpl implements CarDistributionRequestService {

    @Autowired
    private CarDistributionRequestRepository distributionRequestRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private DealerCarRepository dealerCarRepository;

    @Override
    public DistributionRequestResponseDto createDistributionRequest(String userEmail, CreateDistributionRequestDto requestDto) {
        // Tìm user theo email
        UserAccount user = userAccountRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        // Kiểm tra user có thuộc dealer nào không
        if (user.getDealer() == null) {
            throw new RuntimeException("User not associated with any dealer");
        }

        Dealer dealer = user.getDealer();

        // Tìm car theo modelName, variantName và colorName
        Car car = carRepository.findByModelNameAndVariantNameAndColorName(
                requestDto.getModelName(),
                requestDto.getVariantName(),
                requestDto.getColorName())
                .orElseThrow(() -> new RuntimeException(
                    String.format("Car not found with Model: %s, Variant: %s, Color: %s",
                        requestDto.getModelName(),
                        requestDto.getVariantName(),
                        requestDto.getColorName())));

        // Bỏ kiểm tra xe đã có tại dealer - cho phép tạo yêu cầu bổ sung số lượng
        // Đại lý có thể tạo yêu cầu cho xe mới hoặc xe đã có để bổ sung số lượng tồn kho

        // Tạo distribution request mới
        CarDistributionRequest distributionRequest = new CarDistributionRequest();
        distributionRequest.setDealer(dealer);
        distributionRequest.setCar(car);
        distributionRequest.setQuantity(requestDto.getQuantity());
        distributionRequest.setRequestDate(LocalDateTime.now());
        distributionRequest.setStatus("Chờ duyệt");

        // Lưu vào database
        CarDistributionRequest savedRequest = distributionRequestRepository.save(distributionRequest);

        // Chuyển đổi thành response DTO
        return convertToResponseDto(savedRequest);
    }

    @Override
    public List<DistributionRequestResponseDto> getDistributionRequestsByDealer(String userEmail) {
        // Tìm user theo email
        UserAccount user = userAccountRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        // Kiểm tra user có thuộc dealer nào không
        if (user.getDealer() == null) {
            throw new RuntimeException("User not associated with any dealer");
        }

        Integer dealerId = user.getDealer().getDealerId();

        // Lấy tất cả requests của dealer
        List<CarDistributionRequest> requests = distributionRequestRepository.findByDealerId(dealerId);

        // Chuyển đổi thành response DTOs
        return requests.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<DistributionRequestResponseDto> getDistributionRequestsByDealerAndStatus(String userEmail, String status) {
        // Tìm user theo email
        UserAccount user = userAccountRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        // Kiểm tra user có thuộc dealer nào không
        if (user.getDealer() == null) {
            throw new RuntimeException("User not associated with any dealer");
        }

        Integer dealerId = user.getDealer().getDealerId();

        // Lấy requests theo dealer và status
        List<CarDistributionRequest> requests = distributionRequestRepository.findByDealerIdAndStatus(dealerId, status);

        // Chuyển đổi thành response DTOs
        return requests.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<DistributionRequestResponseDto> getAllDistributionRequests() {
        // Lấy tất cả requests trong hệ thống (cho admin/evm staff)
        List<CarDistributionRequest> requests = distributionRequestRepository.findAll();

        // Chuyển đổi thành response DTOs
        return requests.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<DistributionRequestResponseDto> getDistributionRequestsByStatus(String status) {
        // Lấy requests theo status trong hệ thống (cho admin/evm staff)
        List<CarDistributionRequest> requests = distributionRequestRepository.findByStatus(status);

        // Chuyển đổi thành response DTOs
        return requests.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public DistributionRequestResponseDto approveDistributionRequest(Integer requestId, String adminEmail) {
        // Tìm yêu cầu phân phối theo ID
        CarDistributionRequest distributionRequest = distributionRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Distribution request not found with ID: " + requestId));

        // Kiểm tra trạng thái hiện tại - chỉ có thể duyệt yêu cầu đang "Chờ duyệt"
        if (!"Chờ duyệt".equals(distributionRequest.getStatus())) {
            throw new RuntimeException("Can only approve requests with status 'Chờ duyệt'. Current status: " + distributionRequest.getStatus());
        }

        // Cập nhật trạng thái và thời gian duyệt
        distributionRequest.setStatus("Đã duyệt");
        distributionRequest.setApprovedDate(LocalDateTime.now());

        // Lưu thay đổi
        CarDistributionRequest savedRequest = distributionRequestRepository.save(distributionRequest);

        // Trả về response DTO
        return convertToResponseDto(savedRequest);
    }

    @Override
    public DistributionRequestResponseDto rejectDistributionRequest(Integer requestId, String adminEmail) {
        // Tìm yêu cầu phân phối theo ID
        CarDistributionRequest distributionRequest = distributionRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Distribution request not found with ID: " + requestId));

        // Kiểm tra trạng thái hiện tại - chỉ có thể từ chối yêu cầu đang "Chờ duyệt"
        if (!"Chờ duyệt".equals(distributionRequest.getStatus())) {
            throw new RuntimeException("Can only reject requests with status 'Chờ duyệt'. Current status: " + distributionRequest.getStatus());
        }

        // Cập nhật trạng thái
        distributionRequest.setStatus("Từ chối");
        // Note: Có thể thêm field rejectedDate và rejectedBy nếu cần track thông tin này

        // Lưu thay đổi
        CarDistributionRequest savedRequest = distributionRequestRepository.save(distributionRequest);

        // Trả về response DTO
        return convertToResponseDto(savedRequest);
    }

    @Override
    public DistributionRequestResponseDto setDeliveryDateAndStartDelivery(Integer requestId, String evmStaffEmail, LocalDateTime expectedDeliveryDate) {
        // Tìm yêu cầu phân phối theo ID
        CarDistributionRequest distributionRequest = distributionRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Distribution request not found with ID: " + requestId));

        // Kiểm tra trạng thái hiện tại - chỉ có thể set delivery date cho yêu cầu "Đã duyệt"
        if (!"Đã duyệt".equals(distributionRequest.getStatus())) {
            throw new RuntimeException("Can only set delivery date for requests with status 'Đã duyệt'. Current status: " + distributionRequest.getStatus());
        }

        // Kiểm tra ngày giao xe dự kiến phải trong tương lai
        if (expectedDeliveryDate.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Expected delivery date must be in the future");
        }

        // Cập nhật ngày giao xe dự kiến và chuyển status thành "Đang giao"
        distributionRequest.setExpectedDeliveryDate(expectedDeliveryDate);
        distributionRequest.setStatus("Đang giao");

        // Lưu thay đổi
        CarDistributionRequest savedRequest = distributionRequestRepository.save(distributionRequest);

        // Trả về response DTO
        return convertToResponseDto(savedRequest);
    }

    @Override
    public DistributionRequestResponseDto confirmDelivery(Integer requestId, String dealerEmail) {
        // Tìm yêu cầu phân phối theo ID
        CarDistributionRequest distributionRequest = distributionRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Distribution request not found with ID: " + requestId));

        // Kiểm tra trạng thái hiện tại - chỉ có thể xác nhận giao hàng cho yêu cầu đang "Đang giao"
        if (!"Đang giao".equals(distributionRequest.getStatus())) {
            throw new RuntimeException("Can only confirm delivery for requests with status 'Đang giao'. Current status: " + distributionRequest.getStatus());
        }

        // Tìm user và dealer để validate
        UserAccount user = userAccountRepository.findByEmail(dealerEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + dealerEmail));

        if (user.getDealer() == null) {
            throw new RuntimeException("User not associated with any dealer");
        }

        Dealer dealer = user.getDealer();

        // Kiểm tra yêu cầu có phải của dealer hiện tại không
        if (!dealer.getDealerId().equals(distributionRequest.getDealer().getDealerId())) {
            throw new RuntimeException("This request does not belong to your dealer");
        }

        // Cập nhật trạng thái thành "Đã giao" và thiết lập ngày giao thực tế
        distributionRequest.setStatus("Đã giao");
        distributionRequest.setActualDeliveryDate(LocalDateTime.now());

        // Cập nhật số lượng tồn kho vào bảng DealerCar
        updateDealerCarInventory(dealer, distributionRequest.getCar(), distributionRequest.getQuantity());

        // Lưu thay đổi
        CarDistributionRequest savedRequest = distributionRequestRepository.save(distributionRequest);

        // Trả về response DTO
        return convertToResponseDto(savedRequest);
    }

    private void updateDealerCarInventory(Dealer dealer, Car car, Integer deliveredQuantity) {
        // Kiểm tra xem dealer đã có xe này chưa
        Optional<DealerCar> existingDealerCar = dealerCarRepository.findByDealerIdAndCarId(dealer.getDealerId(), car.getCarId());

        if (existingDealerCar.isPresent()) {
            // Xe đã có - cập nhật số lượng
            DealerCar dealerCar = existingDealerCar.get();
            dealerCar.setQuantity(dealerCar.getQuantity() + deliveredQuantity);
            dealerCarRepository.save(dealerCar);
        } else {
            // Xe mới - tạo mới record với status "Pending" và dealerPrice = 0
            DealerCar newDealerCar = new DealerCar();

            // Set composite key fields trực tiếp
            newDealerCar.setDealerId(dealer.getDealerId());
            newDealerCar.setCarId(car.getCarId());

            // Set relationships
            newDealerCar.setDealer(dealer);
            newDealerCar.setCar(car);

            // Set other fields
            newDealerCar.setQuantity(deliveredQuantity);
            newDealerCar.setDealerPrice(BigDecimal.ZERO);
            newDealerCar.setStatus("Pending");

            dealerCarRepository.save(newDealerCar);
        }
    }

    private DistributionRequestResponseDto convertToResponseDto(CarDistributionRequest request) {
        return DistributionRequestResponseDto.builder()
                .requestId(request.getRequestId())
                .dealerName(request.getDealer().getDealerName())
                .modelName(request.getCar().getCarVariant().getCarModel().getModelName())
                .variantName(request.getCar().getCarVariant().getVariantName())
                .colorName(request.getCar().getColor().getColorName())
                .quantity(request.getQuantity())
                .requestDate(request.getRequestDate())
                .status(request.getStatus())
                .approvedDate(request.getApprovedDate())
                .expectedDeliveryDate(request.getExpectedDeliveryDate())
                .actualDeliveryDate(request.getActualDeliveryDate())
                .build();
    }
}
