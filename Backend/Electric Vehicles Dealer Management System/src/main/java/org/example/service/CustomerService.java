package org.example.service;

import org.example.dto.CreateCustomerRequest;
import org.example.dto.CreateCustomerResponse;
import org.example.dto.UpdateCustomerRequest;
import org.example.dto.UpdateCustomerResponse;
import org.example.dto.CustomerResponse;
import org.example.dto.CustomerListResponse;

public interface CustomerService {

    /**
     * Tạo customer mới
     */
    CreateCustomerResponse createCustomer(CreateCustomerRequest request);

    /**
     * Cập nhật thông tin customer với authentication
     */
    UpdateCustomerResponse updateCustomer(Integer customerId, UpdateCustomerRequest request, String email);

    /**
     * Lấy thông tin customer theo ID với authentication
     */
    CustomerResponse getCustomerById(Integer customerId, String email);

    /**
     * Tìm kiếm customer theo số điện thoại với authentication
     */
    CustomerResponse searchCustomerByPhoneNumber(String phoneNumber, String email);

    /**
     * Lấy tất cả khách hàng của đại lý đang đăng nhập
     */
    CustomerListResponse getAllCustomersByDealer(String email);
}
