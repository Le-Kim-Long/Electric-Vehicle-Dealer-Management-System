package org.example.service;

import org.example.dto.CreateCustomerRequest;
import org.example.dto.CreateCustomerResponse;
import org.example.dto.UpdateCustomerRequest;
import org.example.dto.UpdateCustomerResponse;
import org.example.dto.CustomerResponse;
import org.example.dto.OrderResponse;
import org.example.entity.Customer;
import org.example.entity.Orders;
import org.example.entity.UserAccount;
import org.example.repository.CustomerRepository;
import org.example.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    /**
     * Tạo customer mới
     */
    @Transactional
    public CreateCustomerResponse createCustomer(CreateCustomerRequest request) {
        // Validate input
        validateCreateCustomerRequest(request);
        Customer existingCustomer = customerRepository.findByEmailAndPhoneNumber(request.getEmail(), request.getPhoneNumber());
        if(existingCustomer != null) {
            return CreateCustomerResponse.builder()
                    .customerId(existingCustomer.getCustomerId())
                    .message("Customer already exists")
                    .build();
        }

        // Kiểm tra email đã tồn tại chưa
        Optional<Customer> existingCustomerByEmail = customerRepository.findByEmail(request.getEmail());
        if (existingCustomerByEmail.isPresent()) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }

        // Kiểm tra số điện thoại đã tồn tại chưa
        Optional<Customer> existingCustomerByPhone = customerRepository.findByPhoneNumber(request.getPhoneNumber());
        if (existingCustomerByPhone.isPresent()) {
            throw new RuntimeException("Phone number already exists: " + request.getPhoneNumber());
        }

        // Tạo Customer entity
        Customer customer = new Customer();
        customer.setFullName(request.getFullName().trim());
        customer.setEmail(request.getEmail().trim().toLowerCase());
        customer.setPhoneNumber(request.getPhoneNumber().trim());
        customer.setCreatedDate(LocalDateTime.now());

        // Lưu vào database
        Customer savedCustomer = customerRepository.save(customer);

        // Trả về response với customer ID
        return CreateCustomerResponse.builder()
                .customerId(savedCustomer.getCustomerId())
                .message("Customer created successfully")
                .build();
    }

    /**
     * Validate request tạo customer
     */
    private void validateCreateCustomerRequest(CreateCustomerRequest request) {
        if (request == null) {
            throw new RuntimeException("Create customer request cannot be null");
        }

        // Kiểm tra các trường bắt buộc
        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            throw new RuntimeException("Full name is required");
        }

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }

        if (request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty()) {
            throw new RuntimeException("Phone number is required");
        }

        // Validate full name: chỉ chứa chữ cái và khoảng trắng
        String fullName = request.getFullName().trim();
        if (!fullName.matches("^[a-zA-ZàáảãạâấầẩẫậăắằẳẵặèéẻẽẹêếềểễệìíỉĩịòóỏõọôốồổỗộơớờởỡợùúủũụưứừửữựỳýỷỹỵđĐ\\s]+$")) {
            throw new RuntimeException("Full name must contain only letters and spaces");
        }

        // Validate email format
        String email = request.getEmail().trim();
        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            throw new RuntimeException("Invalid email format");
        }

        // Validate phone number: 10 hoặc 11 số
        String phoneNumber = request.getPhoneNumber().trim();
        if (!phoneNumber.matches("^\\d{10,11}$")) {
            throw new RuntimeException("Phone number must be 10 or 11 digits");
        }
    }

    /**
     * Cập nhật thông tin customer
     */
    @Transactional
    public UpdateCustomerResponse updateCustomer(Integer customerId, UpdateCustomerRequest request) {
        // Validate input
        validateUpdateCustomerRequest(request);

        // Tìm customer theo ID
        Optional<Customer> optionalCustomer = customerRepository.findByCustomerId(customerId);
        if (!optionalCustomer.isPresent()) {
            throw new RuntimeException("Customer not found with ID: " + customerId);
        }

        Customer customer = optionalCustomer.get();

        // Kiểm tra email đã tồn tại chưa (ngoại trừ customer hiện tại)
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            Optional<Customer> existingCustomerByEmail = customerRepository.findByEmail(request.getEmail().trim().toLowerCase());
            if (existingCustomerByEmail.isPresent() && !existingCustomerByEmail.get().getCustomerId().equals(customerId)) {
                throw new RuntimeException("Email already exists: " + request.getEmail());
            }
        }

        // Kiểm tra số điện thoại đã tồn tại chưa (ngoại trừ customer hiện tại)
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            Optional<Customer> existingCustomerByPhone = customerRepository.findByPhoneNumber(request.getPhoneNumber().trim());
            if (existingCustomerByPhone.isPresent() && !existingCustomerByPhone.get().getCustomerId().equals(customerId)) {
                throw new RuntimeException("Phone number already exists: " + request.getPhoneNumber());
            }
        }

        // Cập nhật thông tin customer
        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            customer.setFullName(request.getFullName().trim());
        }

        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            customer.setEmail(request.getEmail().trim().toLowerCase());
        }

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            customer.setPhoneNumber(request.getPhoneNumber().trim());
        }

        // Lưu vào database
        Customer updatedCustomer = customerRepository.save(customer);

        // Trả về response
        return UpdateCustomerResponse.builder()
                .customerId(updatedCustomer.getCustomerId())
                .message("Customer updated successfully")
                .build();
    }

    /**
     * Validate request cập nhật customer
     */
    private void validateUpdateCustomerRequest(UpdateCustomerRequest request) {
        if (request == null) {
            throw new RuntimeException("Update customer request cannot be null");
        }

        // Kiểm tra ít nhất một trường được cung cấp
        if ((request.getFullName() == null || request.getFullName().trim().isEmpty()) &&
            (request.getEmail() == null || request.getEmail().trim().isEmpty()) &&
            (request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty())) {
            throw new RuntimeException("At least one field (fullName, email, phoneNumber) must be provided");
        }

        // Validate full name nếu được cung cấp
        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            String fullName = request.getFullName().trim();
            if (!fullName.matches("^[a-zA-ZàáảãạâấầẩẫậăắằẳẵặèéẻẽẹêếềểễệìíỉĩịòóỏõọôốồổỗộơớờởỡợùúủũụưứừửữựỳýỷỹỵđĐ\\s]+$")) {
                throw new RuntimeException("Full name must contain only letters and spaces");
            }
        }

        // Validate email format nếu được cung cấp
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            String email = request.getEmail().trim();
            if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
                throw new RuntimeException("Invalid email format");
            }
        }

        // Validate phone number nếu được cung cấp
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            String phoneNumber = request.getPhoneNumber().trim();
            if (!phoneNumber.matches("^\\d{10,11}$")) {
                throw new RuntimeException("Phone number must be 10 or 11 digits");
            }
        }
    }

    /**
     * Cập nhật thông tin customer với authentication
     */
    @Transactional
    public UpdateCustomerResponse updateCustomer(Integer customerId, UpdateCustomerRequest request, String email) {
        // Validate input
        validateUpdateCustomerRequest(request);

        // Find user by email
        Optional<UserAccount> userOpt = userAccountRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found with email: " + email);
        }

        UserAccount user = userOpt.get();

        // Check if user has dealer access role
        String role = user.getRoleId().getRoleName();
        if (!"DealerManager".equals(role) && !"DealerStaff".equals(role)) {
            throw new RuntimeException("Access denied. Only DealerManager and DealerStaff can update customer information.");
        }

        // Check if user is assigned to a dealer
        if (user.getDealer() == null) {
            throw new RuntimeException("User is not assigned to any dealer");
        }

        // Tìm customer theo ID
        Optional<Customer> optionalCustomer = customerRepository.findByCustomerId(customerId);
        if (!optionalCustomer.isPresent()) {
            throw new RuntimeException("Customer not found with ID: " + customerId);
        }

        Customer customer = optionalCustomer.get();

        // Kiểm tra email đã tồn tại chưa (ngoại trừ customer hiện tại)
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            Optional<Customer> existingCustomerByEmail = customerRepository.findByEmail(request.getEmail().trim().toLowerCase());
            if (existingCustomerByEmail.isPresent() && !existingCustomerByEmail.get().getCustomerId().equals(customerId)) {
                throw new RuntimeException("Email already exists: " + request.getEmail());
            }
        }

        // Kiểm tra số điện thoại đã tồn tại chưa (ngoại trừ customer hiện tại)
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            Optional<Customer> existingCustomerByPhone = customerRepository.findByPhoneNumber(request.getPhoneNumber().trim());
            if (existingCustomerByPhone.isPresent() && !existingCustomerByPhone.get().getCustomerId().equals(customerId)) {
                throw new RuntimeException("Phone number already exists: " + request.getPhoneNumber());
            }
        }

        // Cập nhật thông tin customer
        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            customer.setFullName(request.getFullName().trim());
        }

        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            customer.setEmail(request.getEmail().trim().toLowerCase());
        }

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            customer.setPhoneNumber(request.getPhoneNumber().trim());
        }

        // Lưu vào database
        Customer updatedCustomer = customerRepository.save(customer);

        // Trả về response
        return UpdateCustomerResponse.builder()
                .customerId(updatedCustomer.getCustomerId())
                .message("Customer updated successfully")
                .build();
    }

    /**
     * Lấy thông tin customer theo ID
     */
    public CustomerResponse getCustomerById(Integer customerId) {
        // Tìm customer theo ID
        Optional<Customer> optionalCustomer = customerRepository.findByCustomerId(customerId);
        if (!optionalCustomer.isPresent()) {
            throw new RuntimeException("Customer not found with ID: " + customerId);
        }

        Customer customer = optionalCustomer.get();

        // Trả về CustomerResponse
        return CustomerResponse.builder()
                .customerId(customer.getCustomerId())
                .fullName(customer.getFullName())
                .phoneNumber(customer.getPhoneNumber())
                .email(customer.getEmail())
                .build();
    }

    /**
     * Lấy thông tin customer theo ID với authentication
     */
    public CustomerResponse getCustomerById(Integer customerId, String email) {
        // Find user by email
        Optional<UserAccount> userOpt = userAccountRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found with email: " + email);
        }

        UserAccount user = userOpt.get();

        // Check if user has dealer access role
        String role = user.getRoleId().getRoleName();
        if (!"DealerManager".equals(role) && !"DealerStaff".equals(role)) {
            throw new RuntimeException("Access denied. Only DealerManager and DealerStaff can access customer information.");
        }

        // Check if user is assigned to a dealer
        if (user.getDealer() == null) {
            throw new RuntimeException("User is not assigned to any dealer");
        }

        // Tìm customer theo ID
        Optional<Customer> optionalCustomer = customerRepository.findByCustomerId(customerId);
        if (!optionalCustomer.isPresent()) {
            throw new RuntimeException("Customer not found with ID: " + customerId);
        }

        Customer customer = optionalCustomer.get();

        // Trả về CustomerResponse
        return CustomerResponse.builder()
                .customerId(customer.getCustomerId())
                .fullName(customer.getFullName())
                .phoneNumber(customer.getPhoneNumber())
                .email(customer.getEmail())
                .build();
    }

}

