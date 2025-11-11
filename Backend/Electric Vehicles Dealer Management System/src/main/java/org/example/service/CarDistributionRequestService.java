package org.example.service;

import org.example.dto.CreateDistributionRequestDto;
import org.example.dto.DistributionRequestResponseDto;

import java.time.LocalDateTime;
import java.util.List;

public interface CarDistributionRequestService {

    // Tạo yêu cầu phân phối xe từ dealer manager
    DistributionRequestResponseDto createDistributionRequest(String userEmail, CreateDistributionRequestDto requestDto);

    // Lấy tất cả yêu cầu của dealer hiện tại
    List<DistributionRequestResponseDto> getDistributionRequestsByDealer(String userEmail);

    // Lấy yêu cầu theo status của dealer hiện tại
    List<DistributionRequestResponseDto> getDistributionRequestsByDealerAndStatus(String userEmail, String status);

    // Lấy tất cả yêu cầu trong hệ thống (cho admin/evm staff)
    List<DistributionRequestResponseDto> getAllDistributionRequests();

    // Lấy yêu cầu theo status trong hệ thống (cho admin/evm staff)
    List<DistributionRequestResponseDto> getDistributionRequestsByStatus(String status);

    // Duyệt yêu cầu phân phối xe - chỉ Admin và EVM Staff
    DistributionRequestResponseDto approveDistributionRequest(Integer requestId, String adminEmail);

    // Từ chối yêu cầu phân phối xe - chỉ Admin và EVM Staff
    DistributionRequestResponseDto rejectDistributionRequest(Integer requestId, String adminEmail);

    // Set ngày giao xe dự kiến và chuyển status thành "Đang giao" - chỉ EVM Staff
    DistributionRequestResponseDto setDeliveryDateAndStartDelivery(Integer requestId, String evmStaffEmail, LocalDateTime expectedDeliveryDate);

    // Dealer xác nhận đã nhận xe - chỉ Dealer Manager và Dealer Staff
    DistributionRequestResponseDto confirmDelivery(Integer requestId, String dealerEmail);
}
