package org.example.service.implementation;

import org.example.dto.*;
import org.example.entity.Orders;
import org.example.entity.Payment;
import org.example.entity.UserAccount;
import org.example.repository.OrdersRepository;
import org.example.repository.PaymentRepository;
import org.example.repository.UserAccountRepository;
import org.example.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    /**
     * Tạo hóa đơn thanh toán mới cho "Trả thẳng"
     */
    @Transactional
    public CreatePaymentResponse createPayment(CreatePaymentRequest request, String email) {
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
            throw new RuntimeException("Access denied. Only DealerManager and DealerStaff can create payments.");
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

        // Check if order has total amount > 0
        if (order.getTotalAmount() == null || order.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Order must have a total amount greater than 0 to create payment");
        }

        // Validate payment method
        String method = request.getMethod().trim();
        if (!isValidPaymentMethod(method)) {
            throw new RuntimeException("Invalid payment method: " + method + ". Supported methods: Tiền mặt, Chuyển khoản");
        }

        // Get existing payments for this order
        List<Payment> existingPayments = paymentRepository.findByOrderId(request.getOrderId());

        BigDecimal paymentAmount;
        String orderPaymentMethod = order.getPaymentMethod();

        // Handle different payment methods and validate order status accordingly
        if (orderPaymentMethod != null &&
            (orderPaymentMethod.toLowerCase().contains("trả thẳng") ||
             orderPaymentMethod.toLowerCase().contains("tiền mặt"))) {

            // For "Trả thẳng" orders, status must be "Chưa thanh toán"
            if (order.getStatus() == null || !order.getStatus().equals("Chưa thanh toán")) {
                throw new RuntimeException("Payment for 'Trả thẳng' orders can only be created when order status is 'Chưa thanh toán'. Current status: " + order.getStatus());
            }

            // "Trả thẳng" logic: Only allow 1 payment with full amount
            if (!existingPayments.isEmpty()) {
                throw new RuntimeException("Order with payment method 'Trả thẳng' can only have 1 payment. Payment already exists for this order.");
            }
            paymentAmount = order.getTotalAmount();

        } else {
            throw new RuntimeException("Order payment method must be set to a supported method. Current method: " + orderPaymentMethod +
                                     ". Supported methods: Thanh toán tiền mặt, Thanh toán thẻ tín dụng, Thanh toán chuyển khoản, Thanh toán trả góp");
        }

        // Create payment entity
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(paymentAmount);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setMethod(method);
        payment.setStatus("Chờ xử lý");
        payment.setNote(request.getNote());

        // Save payment
        Payment savedPayment = paymentRepository.save(payment);

        // Create response
        CreatePaymentResponse response = new CreatePaymentResponse();
        response.setPaymentId(savedPayment.getPaymentId());
        response.setOrderId(savedPayment.getOrder().getOrderId());
        response.setAmount(savedPayment.getAmount());
        response.setPaymentDate(savedPayment.getPaymentDate());
        response.setMethod(savedPayment.getMethod());
        response.setStatus(savedPayment.getStatus());
        response.setNote(savedPayment.getNote());
        response.setMessage("Payment created successfully");

        return response;
    }

    /**
     * Cập nhật payment method và reset status về "Chờ xử lý"
     */
    @Transactional
    public UpdatePaymentMethodResponse updatePaymentMethod(Integer paymentId, UpdatePaymentMethodRequest request, String email) {
        // Validate input
        if (paymentId == null) {
            throw new RuntimeException("Payment ID is required");
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
            throw new RuntimeException("Access denied. Only DealerManager and DealerStaff can update payment methods.");
        }

        // Check if user is assigned to a dealer
        if (user.getDealer() == null) {
            throw new RuntimeException("User is not assigned to any dealer");
        }

        // Find payment by ID
        Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
        if (paymentOpt.isEmpty()) {
            throw new RuntimeException("Payment not found with ID: " + paymentId);
        }

        Payment payment = paymentOpt.get();

        // Check if payment's order belongs to user's dealer
        if (!payment.getOrder().getDealer().getDealerId().equals(user.getDealer().getDealerId())) {
            throw new RuntimeException("Access denied. Payment does not belong to your dealer.");
        }

        // Validate new payment method
        String method = request.getMethod().trim();
        if (!isValidPaymentMethod(method)) {
            throw new RuntimeException("Invalid payment method: " + method + ". Supported methods: Tiền mặt");
        }

        // Update payment method and reset status to "Chờ xử lý"
        payment.setMethod(method);
        payment.setStatus("Chờ xử lý");
        if (request.getNote() != null) {
            payment.setNote(request.getNote());
        }

        // Save updated payment
        Payment updatedPayment = paymentRepository.save(payment);

        // Create response
        UpdatePaymentMethodResponse response = new UpdatePaymentMethodResponse();
        response.setPaymentId(updatedPayment.getPaymentId());
        response.setOrderId(updatedPayment.getOrder().getOrderId());
        response.setAmount(updatedPayment.getAmount());
        response.setPaymentDate(updatedPayment.getPaymentDate());
        response.setMethod(updatedPayment.getMethod());
        response.setStatus(updatedPayment.getStatus());
        response.setNote(updatedPayment.getNote());
        response.setMessage("Payment method updated successfully");

        return response;
    }

    /**
     * Cập nhật trạng thái thanh toán và tự động cập nhật trạng thái đơn hàng nếu cần
     */
    @Transactional
    public UpdatePaymentStatusResponse updatePaymentStatus(Integer paymentId, UpdatePaymentStatusRequest request, String email) {
        // Validate input
        if (paymentId == null) {
            throw new RuntimeException("Payment ID is required");
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
            throw new RuntimeException("Access denied. Only DealerManager and DealerStaff can update payment statuses.");
        }

        // Check if user is assigned to a dealer
        if (user.getDealer() == null) {
            throw new RuntimeException("User is not assigned to any dealer");
        }

        // Find payment by ID
        Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
        if (paymentOpt.isEmpty()) {
            throw new RuntimeException("Payment not found with ID: " + paymentId);
        }

        Payment payment = paymentOpt.get();

        // Check if payment's order belongs to user's dealer
        if (!payment.getOrder().getDealer().getDealerId().equals(user.getDealer().getDealerId())) {
            throw new RuntimeException("Access denied. Payment does not belong to your dealer.");
        }

        // Update payment status and note
        payment.setStatus(request.getStatus());
        if (request.getNote() != null) {
            payment.setNote(request.getNote());
        }
        Payment updatedPayment = paymentRepository.save(payment);

        Orders order = updatedPayment.getOrder();
        String orderPaymentMethod = order.getPaymentMethod();
        String orderStatusMessage = "";

        // Auto update order status if payment is completed
        if ("Hoàn thành".equals(request.getStatus())) {
            // For "Trả thẳng" orders - immediately update to "Đã thanh toán"
            if (orderPaymentMethod != null &&
                (orderPaymentMethod.toLowerCase().contains("trả thẳng") ||
                 orderPaymentMethod.toLowerCase().contains("tiền mặt"))) {

                order.setStatus("Đã thanh toán");
                order.setCompletedDate(LocalDateTime.now());
                ordersRepository.save(order);
                orderStatusMessage = "Order status automatically updated to 'Đã thanh toán' for 'Trả thẳng' payment method.";

            }
        }

        // Create response
        UpdatePaymentStatusResponse response = new UpdatePaymentStatusResponse();
        response.setPaymentId(updatedPayment.getPaymentId());
        response.setOrderId(updatedPayment.getOrder().getOrderId());
        response.setAmount(updatedPayment.getAmount());
        response.setPaymentDate(updatedPayment.getPaymentDate());
        response.setMethod(updatedPayment.getMethod());
        response.setStatus(updatedPayment.getStatus());
        response.setNote(updatedPayment.getNote());
        response.setOrderStatus(order.getStatus());

        String message = "Payment status updated successfully.";
        if (!orderStatusMessage.isEmpty()) {
            message += " " + orderStatusMessage;
        }
        response.setMessage(message);

        return response;
    }

    /**
     * Xóa hóa đơn thanh toán
     */
    @Transactional
    public void deletePayment(Integer paymentId, String email) {
        // Validate input
        if (paymentId == null) {
            throw new RuntimeException("Payment ID is required");
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
            throw new RuntimeException("Access denied. Only DealerManager and DealerStaff can delete payments.");
        }

        // Check if user is assigned to a dealer
        if (user.getDealer() == null) {
            throw new RuntimeException("User is not assigned to any dealer");
        }

        // Find payment by ID
        Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
        if (paymentOpt.isEmpty()) {
            throw new RuntimeException("Payment not found with ID: " + paymentId);
        }

        Payment payment = paymentOpt.get();

        // Check if payment's order belongs to user's dealer
        if (!payment.getOrder().getDealer().getDealerId().equals(user.getDealer().getDealerId())) {
            throw new RuntimeException("Access denied. Payment does not belong to your dealer.");
        }

        // Delete payment
        paymentRepository.delete(payment);
    }

    /**
     * Lấy tất cả payments của một order với kiểm tra quyền truy cập
     */
    public List<PaymentResponse> getPaymentsByOrderId(Integer orderId, String email) {
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
            throw new RuntimeException("Access denied. Only DealerManager and DealerStaff can view payments.");
        }

        // Check if user is assigned to a dealer
        if (user.getDealer() == null) {
            throw new RuntimeException("User is not assigned to any dealer");
        }

        // Find order by ID to verify access
        Optional<Orders> orderOpt = ordersRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new RuntimeException("Order not found with ID: " + orderId);
        }

        Orders order = orderOpt.get();

        // Check if order belongs to user's dealer
        if (!order.getDealer().getDealerId().equals(user.getDealer().getDealerId())) {
            throw new RuntimeException("Access denied. Order does not belong to your dealer.");
        }

        // Find payments by order ID
        List<Payment> payments = paymentRepository.findByOrderId(orderId);

        // Convert to response DTOs
        return payments.stream().map(payment -> {
            PaymentResponse response = new PaymentResponse();
            response.setPaymentId(payment.getPaymentId());
            response.setAmount(payment.getAmount());
            response.setPaymentDate(payment.getPaymentDate());
            response.setMethod(payment.getMethod());
            response.setStatus(payment.getStatus());
            response.setNote(payment.getNote());
            return response;
        }).toList();
    }

    /**
     * Validate payment method
     */
    private boolean isValidPaymentMethod(String method) {
        if (method == null || method.trim().isEmpty()) {
            return false;
        }

        String lowerMethod = method.toLowerCase();
        return lowerMethod.equals("tiền mặt");
    }
}
