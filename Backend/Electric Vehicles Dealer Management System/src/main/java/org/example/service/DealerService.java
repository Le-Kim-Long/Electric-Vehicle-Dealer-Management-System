package org.example.service;

import org.example.dto.DealerResponse;
import org.example.entity.Dealer;
import org.example.repository.DealerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DealerService {

    @Autowired
    private DealerRepository dealerRepository;

    /**
     * Lấy tất cả tên dealer (chỉ tên)
     */
    public List<String> getAllDealerNames() {
        return dealerRepository.findAllDealerNames();
    }

    /**
     * Lấy thông tin chi tiết tất cả dealer
     */
    public List<DealerResponse> getAllDealers() {
        List<Dealer> dealers = dealerRepository.findAllDealersOrderByName();

        return dealers.stream()
                .map(dealer -> DealerResponse.builder()
                        .dealerId(dealer.getDealerId())
                        .dealerName(dealer.getDealerName())
                        .address(dealer.getAddress())
                        .phone(dealer.getPhone())
                        .email(dealer.getEmail())
                        .build())
                .collect(Collectors.toList());
    }
}
