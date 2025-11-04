package org.example.service;

import org.example.dto.CreateInstallmentRequest;
import org.example.dto.CreateInstallmentResponse;
import org.example.dto.GetInstallmentByOrderResponse;
import org.example.dto.UpdateInstallmentRequest;
import org.example.dto.UpdateInstallmentResponse;
import org.example.entity.Installment;
import org.example.entity.Orders;
import org.example.entity.UserAccount;
import org.example.repository.InstallmentRepository;
import org.example.repository.OrdersRepository;
import org.example.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
public class InstallmentService {

    @Autowired
    private InstallmentRepository installmentRepository;

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Transactional
    public CreateInstallmentResponse createInstallment(CreateInstallmentRequest request, String email) {
        // Validate input
        if (request.getOrderId() == null) {
            throw new RuntimeException("Order ID is required");
        }

        // Find user by email
        Optional<UserAccount> userOpt = userAccountRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found with email: " + email);
        }

        UserAccount user = userOpt.get();

        // Check if user has dealer access role
        String role = user.getRoleId().getRoleName();
        if (!"DealerManager".equals(role) && !"DealerStaff".equals(role)) {
            throw new RuntimeException("Access denied. Only DealerManager and DealerStaff can create installment plans.");
        }

        // Check if user is assigned to a dealer
        if (user.getDealer() == null) {
            throw new RuntimeException("User is not assigned to any dealer");
        }

        // Find order by ID
        Optional<Orders> orderOpt = ordersRepository.findById(request.getOrderId());
        if (orderOpt.isEmpty()) {
            throw new RuntimeException("Order not found with ID: " + request.getOrderId());
        }

        Orders order = orderOpt.get();

        // Check if order belongs to user's dealer
        if (!order.getDealer().getDealerId().equals(user.getDealer().getDealerId())) {
            throw new RuntimeException("Access denied. Order does not belong to your dealer.");
        }

        // Check if order already has an installment plan
        Optional<Installment> existingInstallment = installmentRepository.findByOrderId(request.getOrderId());
        if (existingInstallment.isPresent()) {
            throw new RuntimeException("Order already has an installment plan");
        }

        // Check if order has total amount > 0
        if (order.getTotalAmount() == null || order.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Order must have a total amount greater than 0 to create installment plan");
        }

        // Check if payment method is installment
        if (order.getPaymentMethod() == null ||
            (!order.getPaymentMethod().toLowerCase().contains("installment") &&
             !order.getPaymentMethod().toLowerCase().contains("trả góp"))) {
            throw new RuntimeException("Order payment method must be set to 'Installment' or 'Trả góp' to create installment plan");
        }

        // Calculate installment details
        BigDecimal principalAmount = order.getTotalAmount();
        Integer termCount = request.getTermCount();
        BigDecimal annualInterestRate = request.getInterestRate();

        // Calculate total interest using simple interest formula for the total period
        // Total Interest = Principal × (Annual Rate / 100) × (Term Count / 12)
        BigDecimal totalInterest = principalAmount
                .multiply(annualInterestRate.divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP))
                .multiply(new BigDecimal(termCount).divide(new BigDecimal("12"), 10, RoundingMode.HALF_UP))
                .setScale(2, RoundingMode.HALF_UP);

        // Calculate total amount to pay
        BigDecimal totalPay = principalAmount.add(totalInterest);

        // Calculate amount per term
        BigDecimal amountPerTerm = totalPay.divide(new BigDecimal(termCount), 2, RoundingMode.HALF_UP);

        // Create installment entity
        Installment installment = new Installment();
        installment.setOrder(order);
        installment.setPrincipalAmount(principalAmount);
        installment.setTermCount(termCount);
        installment.setInterestRate(annualInterestRate);
        installment.setTotalInterest(totalInterest);
        installment.setTotalPay(totalPay);
        installment.setAmountPerTerm(amountPerTerm);
        installment.setNote(request.getNote());

        // Save installment
        Installment savedInstallment = installmentRepository.save(installment);

        // Create response
        CreateInstallmentResponse response = new CreateInstallmentResponse();
        response.setInstallmentId(savedInstallment.getInstallmentId());
        response.setOrderId(savedInstallment.getOrder().getOrderId());
        response.setPrincipalAmount(savedInstallment.getPrincipalAmount());
        response.setTermCount(savedInstallment.getTermCount());
        response.setInterestRate(savedInstallment.getInterestRate());
        response.setTotalInterest(savedInstallment.getTotalInterest());
        response.setTotalPay(savedInstallment.getTotalPay());
        response.setAmountPerTerm(savedInstallment.getAmountPerTerm());
        response.setNote(savedInstallment.getNote());
        response.setMessage("Installment plan created successfully");

        return response;
    }

    public GetInstallmentByOrderResponse getInstallmentByOrderId(Integer orderId, String email) {
        // Validate input
        if (orderId == null) {
            throw new RuntimeException("Order ID is required");
        }

        // Find user by email
        Optional<UserAccount> userOpt = userAccountRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found with email: " + email);
        }

        UserAccount user = userOpt.get();

        // Check if user has dealer access role
        String role = user.getRoleId().getRoleName();
        if (!"DealerManager".equals(role) && !"DealerStaff".equals(role)) {
            throw new RuntimeException("Access denied. Only DealerManager and DealerStaff can access installment information.");
        }

        // Check if user is assigned to a dealer
        if (user.getDealer() == null) {
            throw new RuntimeException("User is not assigned to any dealer");
        }

        // Find order by ID
        Optional<Orders> orderOpt = ordersRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new RuntimeException("Order not found with ID: " + orderId);
        }

        Orders order = orderOpt.get();

        // Check if order belongs to user's dealer
        if (!order.getDealer().getDealerId().equals(user.getDealer().getDealerId())) {
            throw new RuntimeException("Access denied. Order does not belong to your dealer.");
        }

        // Find installment by order ID
        Optional<Installment> installmentOpt = installmentRepository.findByOrderId(orderId);
        if (installmentOpt.isEmpty()) {
            throw new RuntimeException("No installment plan found for order ID: " + orderId);
        }

        Installment installment = installmentOpt.get();

        // Create response
        GetInstallmentByOrderResponse response = new GetInstallmentByOrderResponse();
        response.setInstallmentId(installment.getInstallmentId());
        response.setOrderId(installment.getOrder().getOrderId());
        response.setPrincipalAmount(installment.getPrincipalAmount());
        response.setTermCount(installment.getTermCount());
        response.setInterestRate(installment.getInterestRate());
        response.setTotalInterest(installment.getTotalInterest());
        response.setTotalPay(installment.getTotalPay());
        response.setAmountPerTerm(installment.getAmountPerTerm());
        response.setNote(installment.getNote());
        response.setMessage("Installment information retrieved successfully");

        return response;
    }

    @Transactional
    public UpdateInstallmentResponse updateInstallmentByOrderId(Integer orderId, UpdateInstallmentRequest request, String email) {
        // Validate input
        if (orderId == null) {
            throw new RuntimeException("Order ID is required");
        }

        // Find user by email
        Optional<UserAccount> userOpt = userAccountRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found with email: " + email);
        }

        UserAccount user = userOpt.get();

        // Check if user has dealer access role
        String role = user.getRoleId().getRoleName();
        if (!"DealerManager".equals(role) && !"DealerStaff".equals(role)) {
            throw new RuntimeException("Access denied. Only DealerManager and DealerStaff can update installment plans.");
        }

        // Check if user is assigned to a dealer
        if (user.getDealer() == null) {
            throw new RuntimeException("User is not assigned to any dealer");
        }

        // Find order by ID
        Optional<Orders> orderOpt = ordersRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new RuntimeException("Order not found with ID: " + orderId);
        }

        Orders order = orderOpt.get();

        // Check if order belongs to user's dealer
        if (!order.getDealer().getDealerId().equals(user.getDealer().getDealerId())) {
            throw new RuntimeException("Access denied. Order does not belong to your dealer.");
        }

        // Find installment by order ID
        Optional<Installment> installmentOpt = installmentRepository.findByOrderId(orderId);
        if (installmentOpt.isEmpty()) {
            throw new RuntimeException("No installment plan found for order ID: " + orderId);
        }

        Installment installment = installmentOpt.get();

        // Check if order has total amount > 0 (should not change, but verify)
        if (order.getTotalAmount() == null || order.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Order must have a total amount greater than 0");
        }

        // Calculate new installment details with updated values
        BigDecimal principalAmount = order.getTotalAmount();
        Integer termCount = request.getTermCount();
        BigDecimal annualInterestRate = request.getInterestRate();

        // Calculate total interest using simple interest formula for the total period
        // Total Interest = Principal × (Annual Rate / 100) × (Term Count / 12)
        BigDecimal totalInterest = principalAmount
                .multiply(annualInterestRate.divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP))
                .multiply(new BigDecimal(termCount).divide(new BigDecimal("12"), 10, RoundingMode.HALF_UP))
                .setScale(2, RoundingMode.HALF_UP);

        // Calculate total amount to pay
        BigDecimal totalPay = principalAmount.add(totalInterest);

        // Calculate amount per term
        BigDecimal amountPerTerm = totalPay.divide(new BigDecimal(termCount), 2, RoundingMode.HALF_UP);

        // Update installment entity
        installment.setTermCount(termCount);
        installment.setInterestRate(annualInterestRate);
        installment.setTotalInterest(totalInterest);
        installment.setTotalPay(totalPay);
        installment.setAmountPerTerm(amountPerTerm);
        installment.setNote(request.getNote());

        // Save updated installment
        Installment updatedInstallment = installmentRepository.save(installment);

        // Create response
        UpdateInstallmentResponse response = new UpdateInstallmentResponse();
        response.setInstallmentId(updatedInstallment.getInstallmentId());
        response.setOrderId(updatedInstallment.getOrder().getOrderId());
        response.setPrincipalAmount(updatedInstallment.getPrincipalAmount());
        response.setTermCount(updatedInstallment.getTermCount());
        response.setInterestRate(updatedInstallment.getInterestRate());
        response.setTotalInterest(updatedInstallment.getTotalInterest());
        response.setTotalPay(updatedInstallment.getTotalPay());
        response.setAmountPerTerm(updatedInstallment.getAmountPerTerm());
        response.setNote(updatedInstallment.getNote());
        response.setMessage("Installment plan updated successfully");

        return response;
    }
}
