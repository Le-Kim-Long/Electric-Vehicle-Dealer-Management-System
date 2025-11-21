package org.example.service;

import org.example.dto.DealerResponse;

import java.util.List;

public interface DealerService {

    /**
     * Lấy tất cả tên dealer (chỉ tên)
     */
    List<String> getAllDealerNames();

    /**
     * Lấy thông tin chi tiết tất cả dealer
     */
    List<DealerResponse> getAllDealers();

    /**
     * Lấy thông tin dealer theo ID
     */
    DealerResponse getDealerById(Integer dealerId);
}
