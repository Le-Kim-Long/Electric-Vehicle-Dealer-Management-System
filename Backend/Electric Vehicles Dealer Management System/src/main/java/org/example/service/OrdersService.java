package org.example.service;

import org.example.dto.*;
import org.example.entity.*;
import org.example.repository.OrdersRepository;
import org.example.repository.UserAccountRepository;
import org.example.repository.CustomerRepository;
import org.example.repository.OrderDetailsRepository;
import org.example.repository.DealerCarRepository;
import org.example.repository.PromotionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrdersService {

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderDetailsRepository orderDetailsRepository;

    @Autowired
    private DealerCarRepository dealerCarRepository;

    @Autowired
    private PromotionRepository promotionRepository;

    /**
     * Lấy tất cả orders của dealer mà user đang đăng nhập thuộc về
     * Chỉ áp dụng cho DealerManager và DealerStaff
     */
    @Transactional(readOnly = true)
    public List<DealerOrderResponse> getOrdersByCurrentUserDealer(String currentUserEmail) {
        // Lấy thông tin user hiện tại
        UserAccount currentUser = userAccountRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + currentUserEmail));

        String roleName = currentUser.getRoleId().getRoleName();

        // Kiểm tra role - chỉ cho phép DealerManager và DealerStaff
        if (!"DealerManager".equals(roleName) && !"DealerStaff".equals(roleName)) {
            throw new RuntimeException("Access denied. Only DealerManager and DealerStaff can access dealer orders.");
        }

        // Kiểm tra user có thuộc dealer nào không
        if (currentUser.getDealer() == null) {
            throw new RuntimeException("User is not assigned to any dealer.");
        }

        Integer dealerId = currentUser.getDealer().getDealerId();

        // Lấy tất cả orders của dealer
        List<Orders> orders = ordersRepository.findAllByDealerId(dealerId);

        // Convert sang DTO với order details
        return orders.stream()
                .map(order -> {
                    // Fetch order details riêng
                    Orders orderWithDetails = ordersRepository.findOrderWithDetails(order.getOrderId());
                    return convertToOrderResponse(orderWithDetails);
                })
                .collect(Collectors.toList());
    }

    /**
     * Convert Orders entity sang DealerOrderResponse DTO
     */
    private DealerOrderResponse convertToOrderResponse(Orders order) {
        // Customer Info
        CustomerInfoResponse customerInfo = null;
        if (order.getCustomer() != null) {
            customerInfo = CustomerInfoResponse.builder()
                    .customerId(order.getCustomer().getCustomerId())
                    .customerName(order.getCustomer().getFullName())
                    .customerPhone(order.getCustomer().getPhoneNumber())
                    .customerEmail(order.getCustomer().getEmail())
                    .build();
        }

        // Dealer Info
        DealerInfoResponse dealerInfo = null;
        if (order.getDealer() != null) {
            dealerInfo = DealerInfoResponse.builder()
                    .dealerId(order.getDealer().getDealerId())
                    .dealerName(order.getDealer().getDealerName())
                    .dealerAddress(order.getDealer().getAddress())
                    .dealerPhone(order.getDealer().getPhone())
                    .build();
        }

        // Order Info
        OrderInfoResponse orderInfo = OrderInfoResponse.builder()
                .orderId(order.getOrderId())
                .orderDate(order.getOrderDate())
                .subTotal(order.getSubTotal())
                .discountAmount(order.getDiscountAmount())
                .totalAmount(order.getTotalAmount())
                .paymentMethod(order.getPaymentMethod())
                .status(order.getStatus())
                .promotionId(order.getPromotion() != null ? order.getPromotion().getPromotionId() : null)
                .promotionName(order.getPromotion() != null ? order.getPromotion().getPromotionName() : null)
                .build();

        // Order Details
        List<OrderDetailResponse> orderDetailResponses = null;
        if (order.getOrderDetails() != null && !order.getOrderDetails().isEmpty()) {
            orderDetailResponses = order.getOrderDetails().stream()
                    .map(this::convertToOrderDetailResponse)
                    .collect(Collectors.toList());
        }

        return DealerOrderResponse.builder()
                .orderInfo(orderInfo)
                .customer(customerInfo)
                .dealer(dealerInfo)
                .orderDetails(orderDetailResponses)
                .build();
    }

    /**
     * Convert OrderDetails entity sang OrderDetailResponse DTO
     */
    private OrderDetailResponse convertToOrderDetailResponse(OrderDetails detail) {
        String carName = null;
        String modelName = null;
        String variantName = null;
        String colorName = null;

        if (detail.getCar() != null) {
            // Get color name
            if (detail.getCar().getColor() != null) {
                colorName = detail.getCar().getColor().getColorName();
            }

            if (detail.getCar().getCarVariant() != null) {
                variantName = detail.getCar().getCarVariant().getVariantName();
                if (detail.getCar().getCarVariant().getCarModel() != null) {
                    modelName = detail.getCar().getCarVariant().getCarModel().getModelName();
                    carName = modelName + " " + variantName;
                }
            }
        }

        return OrderDetailResponse.builder()
                .orderDetailId(detail.getOrderDetailId())
                .orderId(detail.getOrder() != null ? detail.getOrder().getOrderId() : null)
                .carId(detail.getCar() != null ? detail.getCar().getCarId() : null)
                .carName(carName)
                .modelName(modelName)
                .variantName(variantName)
                .colorName(colorName) // Add colorName field
                .quantity(detail.getQuantity())
                .unitPrice(detail.getUnitPrice())
                .finalPrice(detail.getFinalPrice())
                .build();
    }

    /**
     * Tạo bản nháp order cho dealer manager/staff
     */
    @Transactional
    public CreateDraftOrderResponse createDraftOrder(CreateDraftOrderRequest request, String currentUserEmail) {
        // Validate input
        if (request == null || request.getCustomerId() == null) {
            throw new RuntimeException("Customer ID is required");
        }

        // Lấy thông tin user hiện tại
        UserAccount currentUser = userAccountRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + currentUserEmail));

        String roleName = currentUser.getRoleId().getRoleName();

        // Kiểm tra role - chỉ cho phép DealerManager và DealerStaff
        if (!"DealerManager".equals(roleName) && !"DealerStaff".equals(roleName)) {
            throw new RuntimeException("Access denied. Only DealerManager and DealerStaff can create draft orders.");
        }

        // Kiểm tra user có thuộc dealer nào không
        if (currentUser.getDealer() == null) {
            throw new RuntimeException("User is not assigned to any dealer.");
        }

        Dealer dealer = currentUser.getDealer();

        // Kiểm tra customer có tồn tại không
        Customer customer = customerRepository.findByCustomerId(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + request.getCustomerId()));

        // Tạo draft order
        Orders draftOrder = new Orders();
        draftOrder.setCustomer(customer);
        draftOrder.setDealer(dealer);
        draftOrder.setOrderDate(LocalDateTime.now());
        draftOrder.setSubTotal(BigDecimal.ZERO);
        draftOrder.setDiscountAmount(BigDecimal.ZERO);
        draftOrder.setStatus("Chưa xác nhận");
        draftOrder.setPaymentMethod(null); // Chưa có payment method
        draftOrder.setPromotion(null); // Chưa có promotion

        // Lưu vào database
        Orders savedOrder = ordersRepository.save(draftOrder);

        // Trả về response
        return CreateDraftOrderResponse.builder()
                .orderId(savedOrder.getOrderId())
                .customerId(customer.getCustomerId())
                .customerName(customer.getFullName())
                .dealerId(dealer.getDealerId())
                .dealerName(dealer.getDealerName())
                .orderDate(savedOrder.getOrderDate())
                .status(savedOrder.getStatus())
                .message("Draft order created successfully")
                .build();
    }

    /**
     * Tạo order detail với unit price tự động từ dealer price
     */
    @Transactional
    public CreateOrderDetailResponse createOrderDetail(CreateOrderDetailRequest request, String currentUserEmail) {
        // Validate input
        validateCreateOrderDetailRequest(request);

        // Lấy thông tin user hiện tại
        UserAccount currentUser = userAccountRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + currentUserEmail));

        String roleName = currentUser.getRoleId().getRoleName();

        // Kiểm tra role - chỉ cho phép DealerManager và DealerStaff
        if (!"DealerManager".equals(roleName) && !"DealerStaff".equals(roleName)) {
            throw new RuntimeException("Access denied. Only DealerManager and DealerStaff can create order details.");
        }

        // Kiểm tra user có thuộc dealer nào không
        if (currentUser.getDealer() == null) {
            throw new RuntimeException("User is not assigned to any dealer.");
        }

        Integer dealerId = currentUser.getDealer().getDealerId();

        // Kiểm tra order có tồn tại không và thuộc dealer hiện tại
        Orders order = ordersRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + request.getOrderId()));

        if (!order.getDealer().getDealerId().equals(dealerId)) {
            throw new RuntimeException("Access denied. This order does not belong to your dealer.");
        }

        // Tìm car dựa trên modelName, variantName, colorName trong dealer
        DealerCar dealerCar = dealerCarRepository.findByModelVariantColorAndDealer(
                dealerId,
                request.getModelName(),
                request.getVariantName(),
                request.getColorName())
                .orElseThrow(() -> new RuntimeException(
                        String.format("Car not found in dealer inventory. Model: %s, Variant: %s, Color: %s",
                                request.getModelName(), request.getVariantName(), request.getColorName())));

        // Kiểm tra số lượng trong kho
        if (dealerCar.getQuantity() < request.getQuantity()) {
            throw new RuntimeException(
                    String.format("Insufficient inventory. Available: %d, Requested: %d",
                            dealerCar.getQuantity(), request.getQuantity()));
        }

        // Tạo order detail
        OrderDetails orderDetail = new OrderDetails();
        orderDetail.setOrder(order);
        orderDetail.setCar(dealerCar.getCar());
        orderDetail.setQuantity(request.getQuantity());

        // Set unit price từ dealer price
        orderDetail.setUnitPrice(dealerCar.getDealerPrice().doubleValue());

        // Lưu vào database
        OrderDetails savedOrderDetail = orderDetailsRepository.save(orderDetail);

        // Refresh entity để lấy finalPrice được tính toán từ database
        orderDetailsRepository.flush();
        OrderDetails refreshedOrderDetail = orderDetailsRepository.findById(savedOrderDetail.getOrderDetailId())
                .orElse(savedOrderDetail);

        // Nếu finalPrice vẫn null, tính toán thủ công
        BigDecimal finalPrice = refreshedOrderDetail.getFinalPrice();
        if (finalPrice == null) {
            finalPrice = BigDecimal.valueOf(refreshedOrderDetail.getUnitPrice())
                    .multiply(BigDecimal.valueOf(refreshedOrderDetail.getQuantity()));
        }

        // Cập nhật số lượng trong kho dealer
        dealerCar.setQuantity(dealerCar.getQuantity() - request.getQuantity());
        dealerCarRepository.save(dealerCar);

        // Cập nhật subTotal trong bảng Orders
        updateOrderSubTotal(order.getOrderId());

        // Trả về response
        return CreateOrderDetailResponse.builder()
                .orderDetailId(refreshedOrderDetail.getOrderDetailId())
                .orderId(refreshedOrderDetail.getOrder().getOrderId())
                .carId(refreshedOrderDetail.getCar().getCarId())
                .modelName(dealerCar.getCar().getCarVariant().getCarModel().getModelName())
                .variantName(dealerCar.getCar().getCarVariant().getVariantName())
                .colorName(dealerCar.getCar().getColor().getColorName())
                .quantity(refreshedOrderDetail.getQuantity())
                .unitPrice(BigDecimal.valueOf(refreshedOrderDetail.getUnitPrice()))
                .finalPrice(finalPrice) // Sử dụng finalPrice đã tính toán
                .message("Order detail created successfully")
                .build();
    }

    /**
     * Validate request tạo order detail
     */
    private void validateCreateOrderDetailRequest(CreateOrderDetailRequest request) {
        if (request == null) {
            throw new RuntimeException("Create order detail request cannot be null");
        }

        if (request.getOrderId() == null) {
            throw new RuntimeException("Order ID is required");
        }

        if (request.getModelName() == null || request.getModelName().trim().isEmpty()) {
            throw new RuntimeException("Model name is required");
        }

        if (request.getVariantName() == null || request.getVariantName().trim().isEmpty()) {
            throw new RuntimeException("Variant name is required");
        }

        if (request.getColorName() == null || request.getColorName().trim().isEmpty()) {
            throw new RuntimeException("Color name is required");
        }

        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new RuntimeException("Quantity must be greater than 0");
        }
    }

    /**
     * Cập nhật subTotal trong bảng Orders sau khi thêm order detail
     */
    @Transactional
    public void updateOrderSubTotal(Integer orderId) {
        // Tính toán lại subTotal
        BigDecimal newSubTotal = orderDetailsRepository.sumOrderDetailsSubTotal(orderId);

        // Cập nhật subTotal cho order
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        order.setSubTotal(newSubTotal);

        // Lưu thay đổi
        ordersRepository.save(order);
    }

    /**
     * Xóa order detail và hoàn trả số lượng xe cho đại lý
     */
    @Transactional
    public void deleteOrderDetail(Integer orderDetailId, String currentUserEmail) {
        // Lấy thông tin user hiện tại
        UserAccount currentUser = userAccountRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + currentUserEmail));

        String roleName = currentUser.getRoleId().getRoleName();

        // Kiểm tra role - chỉ cho phép DealerManager và DealerStaff
        if (!"DealerManager".equals(roleName) && !"DealerStaff".equals(roleName)) {
            throw new RuntimeException("Access denied. Only DealerManager and DealerStaff can delete order details.");
        }

        // Kiểm tra user có thuộc dealer nào không
        if (currentUser.getDealer() == null) {
            throw new RuntimeException("User is not assigned to any dealer.");
        }

        Integer dealerId = currentUser.getDealer().getDealerId();

        // Tìm order detail
        OrderDetails orderDetail = orderDetailsRepository.findById(orderDetailId)
                .orElseThrow(() -> new RuntimeException("Order detail not found with ID: " + orderDetailId));

        // Kiểm tra order có thuộc dealer hiện tại không
        if (!orderDetail.getOrder().getDealer().getDealerId().equals(dealerId)) {
            throw new RuntimeException("Access denied. This order detail does not belong to your dealer.");
        }

        // Lưu lại thông tin để hoàn trả số lượng
        Integer carId = orderDetail.getCar().getCarId();
        Integer quantityToReturn = orderDetail.getQuantity();
        Integer orderId = orderDetail.getOrder().getOrderId();

        // Tìm DealerCar để hoàn trả số lượng
        DealerCar dealerCar = dealerCarRepository.findByCarIdAndDealerId(carId, dealerId)
                .orElseThrow(() -> new RuntimeException("Dealer car record not found"));

        // Hoàn trả số lượng xe về kho dealer
        dealerCar.setQuantity(dealerCar.getQuantity() + quantityToReturn);
        dealerCarRepository.save(dealerCar);

        // Xóa order detail
        orderDetailsRepository.delete(orderDetail);

        // Cập nhật subTotal trong bảng Orders
        updateOrderSubTotal(orderId);
    }

    /**
     * Cập nhật order detail với logic tính toán tồn kho và subTotal
     */
    @Transactional
    public UpdateOrderDetailResponse updateOrderDetail(Integer orderDetailId, UpdateOrderDetailRequest request, String currentUserEmail) {
        // Validate input
        validateUpdateOrderDetailRequest(request);

        // Lấy thông tin user hiện tại
        UserAccount currentUser = userAccountRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + currentUserEmail));

        String roleName = currentUser.getRoleId().getRoleName();

        // Kiểm tra role - chỉ cho phép DealerManager và DealerStaff
        if (!"DealerManager".equals(roleName) && !"DealerStaff".equals(roleName)) {
            throw new RuntimeException("Access denied. Only DealerManager and DealerStaff can update order details.");
        }

        // Kiểm tra user có thuộc dealer nào không
        if (currentUser.getDealer() == null) {
            throw new RuntimeException("User is not assigned to any dealer.");
        }

        Integer dealerId = currentUser.getDealer().getDealerId();

        // Tìm order detail
        OrderDetails orderDetail = orderDetailsRepository.findById(orderDetailId)
                .orElseThrow(() -> new RuntimeException("Order detail not found with ID: " + orderDetailId));

        // Kiểm tra order có thuộc dealer hiện tại không
        if (!orderDetail.getOrder().getDealer().getDealerId().equals(dealerId)) {
            throw new RuntimeException("Access denied. This order detail does not belong to your dealer.");
        }

        // Lưu số lượng cũ để tính toán
        Integer oldQuantity = orderDetail.getQuantity();
        Integer newQuantity = request.getQuantity();
        Integer quantityDifference = newQuantity - oldQuantity;

        // Tìm DealerCar để kiểm tra và cập nhật tồn kho
        Integer carId = orderDetail.getCar().getCarId();
        DealerCar dealerCar = dealerCarRepository.findByCarIdAndDealerId(carId, dealerId)
                .orElseThrow(() -> new RuntimeException("Dealer car record not found"));

        // Kiểm tra tồn kho nếu tăng số lượng
        if (quantityDifference > 0) {
            if (dealerCar.getQuantity() < quantityDifference) {
                throw new RuntimeException(
                    String.format("Insufficient inventory. Available: %d, Additional needed: %d",
                    dealerCar.getQuantity(), quantityDifference));
            }
        }

        // Cập nhật tồn kho dealer
        // Nếu tăng số lượng: trừ thêm từ kho
        // Nếu giảm số lượng: hoàn trả về kho
        dealerCar.setQuantity(dealerCar.getQuantity() - quantityDifference);
        dealerCarRepository.save(dealerCar);

        // Cập nhật order detail
        orderDetail.setQuantity(newQuantity);
        OrderDetails savedOrderDetail = orderDetailsRepository.save(orderDetail);

        // Flush để đảm bảo changes được persist xuống database
        orderDetailsRepository.flush();

        // Tính toán finalPrice chính xác dựa trên quantity mới
        BigDecimal calculatedFinalPrice = BigDecimal.valueOf(savedOrderDetail.getUnitPrice())
                .multiply(BigDecimal.valueOf(newQuantity));

        // Cập nhật finalPrice trực tiếp trong database nếu cần
        savedOrderDetail.setFinalPrice(calculatedFinalPrice);
        OrderDetails finalOrderDetail = orderDetailsRepository.save(savedOrderDetail);

        // Flush lại để đảm bảo finalPrice được cập nhật
        orderDetailsRepository.flush();

        // Cập nhật subTotal trong bảng Orders
        updateOrderSubTotal(orderDetail.getOrder().getOrderId());

        // Trả về response với finalPrice đã được tính toán chính xác
        return UpdateOrderDetailResponse.builder()
                .orderDetailId(finalOrderDetail.getOrderDetailId())
                .orderId(finalOrderDetail.getOrder().getOrderId())
                .carId(finalOrderDetail.getCar().getCarId())
                .modelName(finalOrderDetail.getCar().getCarVariant().getCarModel().getModelName())
                .variantName(finalOrderDetail.getCar().getCarVariant().getVariantName())
                .colorName(finalOrderDetail.getCar().getColor().getColorName())
                .oldQuantity(oldQuantity)
                .newQuantity(finalOrderDetail.getQuantity())
                .unitPrice(BigDecimal.valueOf(finalOrderDetail.getUnitPrice()))
                .finalPrice(calculatedFinalPrice) // Sử dụng giá trị đã tính toán chính xác
                .message("Order detail updated successfully")
                .build();
    }

    /**
     * Validate request cập nhật order detail
     */
    private void validateUpdateOrderDetailRequest(UpdateOrderDetailRequest request) {
        if (request == null) {
            throw new RuntimeException("Update order detail request cannot be null");
        }

        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new RuntimeException("Quantity must be greater than 0");
        }
    }

    /**
     * Cập nhật promotion cho order với logic tính toán discount và total amount
     */
    @Transactional
    public UpdateOrderPromotionResponse updateOrderPromotion(Integer orderId, UpdateOrderPromotionRequest request, String currentUserEmail) {
        // Validate input
        validateUpdateOrderPromotionRequest(orderId, request);

        // Lấy thông tin user hiện tại
        UserAccount currentUser = userAccountRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + currentUserEmail));

        String roleName = currentUser.getRoleId().getRoleName();

        // Kiểm tra role - chỉ cho phép DealerManager và DealerStaff
        if (!"DealerManager".equals(roleName) && !"DealerStaff".equals(roleName)) {
            throw new RuntimeException("Access denied. Only DealerManager and DealerStaff can update order promotion.");
        }

        // Kiểm tra user có thuộc dealer nào không
        if (currentUser.getDealer() == null) {
            throw new RuntimeException("User is not assigned to any dealer.");
        }

        Integer dealerId = currentUser.getDealer().getDealerId();

        // Tìm order và kiểm tra thuộc dealer hiện tại
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        if (!order.getDealer().getDealerId().equals(dealerId)) {
            throw new RuntimeException("Access denied. This order does not belong to your dealer.");
        }

        // Lấy subTotal hiện tại của order
        BigDecimal subTotal = order.getSubTotal();
        if (subTotal == null || subTotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Order subTotal must be greater than 0 to apply promotion");
        }

        // Nếu promotionId là null, xóa promotion khỏi order
        if (request.getPromotionId() == null) {
            order.setPromotion(null);
            order.setDiscountAmount(BigDecimal.ZERO);

            Orders savedOrder = ordersRepository.save(order);

            return UpdateOrderPromotionResponse.builder()
                    .orderId(savedOrder.getOrderId())
                    .promotionId(null)
                    .promotionName(null)
                    .promotionType(null)
                    .promotionValue(null)
                    .subTotal(subTotal)
                    .discountAmount(BigDecimal.ZERO)
                    .totalAmount(subTotal) // totalAmount = subTotal khi không có promotion
                    .message("Order promotion removed successfully")
                    .build();
        }

        // Tìm promotion và kiểm tra thuộc dealer hiện tại
        Promotion promotion = promotionRepository.findByPromotionIdAndDealerId(request.getPromotionId(), dealerId);
        if (promotion == null) {
            throw new RuntimeException("Promotion not found or does not belong to your dealer with ID: " + request.getPromotionId());
        }

        // Kiểm tra promotion có active không
        if (!"Đang hoạt động".equalsIgnoreCase(promotion.getStatus())) {
            throw new RuntimeException("Promotion is not active. Status: " + promotion.getStatus());
        }

        // Kiểm tra promotion có trong thời hạn không
        java.time.LocalDate currentDate = java.time.LocalDate.now();
        if (promotion.getStartDate().isAfter(currentDate) || promotion.getEndDate().isBefore(currentDate)) {
            throw new RuntimeException("Promotion is not valid for current date. Valid from " +
                promotion.getStartDate() + " to " + promotion.getEndDate());
        }

        // Tính toán discountAmount và totalAmount dựa trên type của promotion
        BigDecimal discountAmount;
        BigDecimal totalAmount;

        if ("VND".equalsIgnoreCase(promotion.getType())) {
            // Type VND: discountAmount = promotion value, totalAmount = subTotal - discountAmount
            discountAmount = promotion.getValue();
            totalAmount = subTotal.subtract(discountAmount);
        } else if ("%".equals(promotion.getType())) {
            // Type %: discountAmount = subTotal * (promotion value / 100), totalAmount = subTotal - discountAmount
            BigDecimal percentage = promotion.getValue().divide(BigDecimal.valueOf(100), 4, BigDecimal.ROUND_HALF_UP);
            discountAmount = subTotal.multiply(percentage);
            totalAmount = subTotal.subtract(discountAmount);
        } else {
            throw new RuntimeException("Invalid promotion type: " + promotion.getType() + ". Supported types are 'VND' and '%'");
        }

        // Đảm bảo totalAmount không âm
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Total amount cannot be negative after applying promotion");
        }

        // Cập nhật order
        order.setPromotion(promotion);
        order.setDiscountAmount(discountAmount);

        // Lưu order
        Orders savedOrder = ordersRepository.save(order);

        // Trả về response
        return UpdateOrderPromotionResponse.builder()
                .orderId(savedOrder.getOrderId())
                .promotionId(promotion.getPromotionId())
                .promotionName(promotion.getPromotionName())
                .promotionType(promotion.getType())
                .promotionValue(promotion.getValue())
                .subTotal(subTotal)
                .discountAmount(discountAmount)
                .totalAmount(totalAmount)
                .message("Order promotion updated successfully")
                .build();
    }

    /**
     * Validate request cập nhật promotion cho order
     */
    private void validateUpdateOrderPromotionRequest(Integer orderId, UpdateOrderPromotionRequest request) {
        if (orderId == null) {
            throw new RuntimeException("Order ID is required");
        }

        if (request == null) {
            throw new RuntimeException("Update order promotion request cannot be null");
        }

        // promotionId có thể null (để xóa promotion)
        // Không cần validate promotionId
    }

    /**
     * Cập nhật payment method cho order
     */
    @Transactional
    public UpdateOrderPaymentMethodResponse updateOrderPaymentMethod(Integer orderId, UpdateOrderPaymentMethodRequest request, String currentUserEmail) {
        // Validate input
        validateUpdateOrderPaymentMethodRequest(orderId, request);

        // Lấy thông tin user hiện tại
        UserAccount currentUser = userAccountRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + currentUserEmail));

        String roleName = currentUser.getRoleId().getRoleName();

        // Kiểm tra role - chỉ cho phép DealerManager và DealerStaff
        if (!"DealerManager".equals(roleName) && !"DealerStaff".equals(roleName)) {
            throw new RuntimeException("Access denied. Only DealerManager and DealerStaff can update order payment method.");
        }

        // Kiểm tra user có thuộc dealer nào không
        if (currentUser.getDealer() == null) {
            throw new RuntimeException("User is not assigned to any dealer.");
        }

        Integer dealerId = currentUser.getDealer().getDealerId();

        // Tìm order và kiểm tra thuộc dealer hiện tại
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        if (!order.getDealer().getDealerId().equals(dealerId)) {
            throw new RuntimeException("Access denied. This order does not belong to your dealer.");
        }

        // Cập nhật payment method
        order.setPaymentMethod(request.getPaymentMethod());

        // Lưu order
        Orders savedOrder = ordersRepository.save(order);

        // Trả về response
        return UpdateOrderPaymentMethodResponse.builder()
                .orderId(savedOrder.getOrderId())
                .paymentMethod(savedOrder.getPaymentMethod())
                .status(savedOrder.getStatus())
                .message("Order payment method updated successfully")
                .build();
    }

    /**
     * Validate request cập nhật payment method cho order
     */
    private void validateUpdateOrderPaymentMethodRequest(Integer orderId, UpdateOrderPaymentMethodRequest request) {
        if (orderId == null) {
            throw new RuntimeException("Order ID is required");
        }

        if (request == null) {
            throw new RuntimeException("Update order payment method request cannot be null");
        }

        if (request.getPaymentMethod() == null || request.getPaymentMethod().trim().isEmpty()) {
            throw new RuntimeException("Payment method is required");
        }

        // Validate payment method values
        String paymentMethod = request.getPaymentMethod().trim();
        if (!isValidPaymentMethod(paymentMethod)) {
            throw new RuntimeException("Invalid payment method. Supported methods: Credit Card, Cash, Bank Transfer, Installment");
        }
    }

    /**
     * Kiểm tra payment method có hợp lệ không
     */
    private boolean isValidPaymentMethod(String paymentMethod) {
        // Các payment method hợp lệ
        String[] validMethods = {"Trả thẳng", "Trả góp"};

        for (String validMethod : validMethods) {
            if (validMethod.equalsIgnoreCase(paymentMethod)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Cập nhật status cho order
     */
    @Transactional
    public UpdateOrderStatusResponse updateOrderStatus(Integer orderId, UpdateOrderStatusRequest request, String currentUserEmail) {
        // Validate input
        validateUpdateOrderStatusRequest(orderId, request);

        // Lấy thông tin user hiện tại
        UserAccount currentUser = userAccountRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + currentUserEmail));

        String roleName = currentUser.getRoleId().getRoleName();

        // Kiểm tra role - chỉ cho phép DealerManager và DealerStaff
        if (!"DealerManager".equals(roleName) && !"DealerStaff".equals(roleName)) {
            throw new RuntimeException("Access denied. Only DealerManager and DealerStaff can update order status.");
        }

        // Kiểm tra user có thuộc dealer nào không
        if (currentUser.getDealer() == null) {
            throw new RuntimeException("User is not assigned to any dealer.");
        }

        Integer dealerId = currentUser.getDealer().getDealerId();

        // Tìm order và kiểm tra thuộc dealer hiện tại
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        if (!order.getDealer().getDealerId().equals(dealerId)) {
            throw new RuntimeException("Access denied. This order does not belong to your dealer.");
        }

        // Lưu status cũ để trả về trong response
        String oldStatus = order.getStatus();

        // Cập nhật status
        order.setStatus(request.getStatus());

        // Lưu order
        Orders savedOrder = ordersRepository.save(order);

        // Trả về response
        return UpdateOrderStatusResponse.builder()
                .orderId(savedOrder.getOrderId())
                .oldStatus(oldStatus)
                .newStatus(savedOrder.getStatus())
                .orderDate(savedOrder.getOrderDate())
                .totalAmount(savedOrder.getTotalAmount())
                .customerName(savedOrder.getCustomer() != null ? savedOrder.getCustomer().getFullName() : null)
                .dealerName(savedOrder.getDealer() != null ? savedOrder.getDealer().getDealerName() : null)
                .message("Order status updated successfully")
                .build();
    }

    /**
     * Validate request cập nhật status cho order
     */
    private void validateUpdateOrderStatusRequest(Integer orderId, UpdateOrderStatusRequest request) {
        if (orderId == null) {
            throw new RuntimeException("Order ID is required");
        }

        if (request == null) {
            throw new RuntimeException("Update order status request cannot be null");
        }

        if (request.getStatus() == null || request.getStatus().trim().isEmpty()) {
            throw new RuntimeException("Status is required");
        }

        // Validate status values
        String status = request.getStatus().trim();
        if (!isValidOrderStatus(status)) {
            throw new RuntimeException("Invalid order status. Supported statuses: Chưa xác nhận, Đang xử lý, Chưa thanh toán, Đã thanh toán, Đã hủy");
        }
    }

    /**
     * Kiểm tra order status có hợp lệ không
     */
    private boolean isValidOrderStatus(String status) {
        // Các order status hợp lệ
        String[] validStatuses = {"Chưa xác nhận", "Đã xác nhận", "Đang xử lý", "Chưa thanh toán","Đang trả góp","Đã thanh toán", "Đã hủy"};

        for (String validStatus : validStatuses) {
            if (validStatus.equalsIgnoreCase(status)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Lấy thông tin đơn hàng theo order ID
     * Chỉ áp dụng cho DealerManager và DealerStaff thuộc dealer sở hữu order
     */
    @Transactional(readOnly = true)
    public DealerOrderResponse getOrderById(Integer orderId, String currentUserEmail) {
        // Validate input
        if (orderId == null) {
            throw new RuntimeException("Order ID is required");
        }

        // Lấy thông tin user hiện tại
        UserAccount currentUser = userAccountRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + currentUserEmail));

        String roleName = currentUser.getRoleId().getRoleName();

        // Kiểm tra role - chỉ cho phép DealerManager và DealerStaff
        if (!"DealerManager".equals(roleName) && !"DealerStaff".equals(roleName)) {
            throw new RuntimeException("Access denied. Only DealerManager and DealerStaff can access order details.");
        }

        // Kiểm tra user có thuộc dealer nào không
        if (currentUser.getDealer() == null) {
            throw new RuntimeException("User is not assigned to any dealer.");
        }

        Integer dealerId = currentUser.getDealer().getDealerId();

        // Tìm order với đầy đủ thông tin order details
        Orders order = ordersRepository.findOrderWithDetails(orderId);

        if (order == null) {
            throw new RuntimeException("Order not found with ID: " + orderId);
        }

        // Kiểm tra order có thuộc dealer hiện tại không
        if (!dealerId.equals(order.getDealer().getDealerId())) {
            throw new RuntimeException("Access denied. This order does not belong to your dealer.");
        }

        // Convert sang DTO với order details
        return convertToOrderResponse(order);
    }

    /**
     * Lấy danh sách order details theo order ID
     * Chỉ áp dụng cho DealerManager và DealerStaff thuộc dealer sở hữu order
     */
    @Transactional(readOnly = true)
    public List<OrderDetailResponse> getOrderDetailsByOrderId(Integer orderId, String currentUserEmail) {
        // Validate input
        if (orderId == null) {
            throw new RuntimeException("Order ID is required");
        }

        // Lấy thông tin user hiện tại
        UserAccount currentUser = userAccountRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + currentUserEmail));

        String roleName = currentUser.getRoleId().getRoleName();

        // Kiểm tra role - chỉ cho phép DealerManager và DealerStaff
        if (!"DealerManager".equals(roleName) && !"DealerStaff".equals(roleName)) {
            throw new RuntimeException("Access denied. Only DealerManager and DealerStaff can access order details.");
        }

        // Kiểm tra user có thuộc dealer nào không
        if (currentUser.getDealer() == null) {
            throw new RuntimeException("User is not assigned to any dealer.");
        }

        Integer dealerId = currentUser.getDealer().getDealerId();

        // Tìm order để kiểm tra quyền truy cập
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        // Kiểm tra order có thuộc dealer hiện tại không
        if (!dealerId.equals(order.getDealer().getDealerId())) {
            throw new RuntimeException("Access denied. This order does not belong to your dealer.");
        }

        // Lấy danh sách order details của order
        List<OrderDetails> orderDetailsList = orderDetailsRepository.findByOrderId(orderId);

        // Convert sang DTO
        return orderDetailsList.stream()
                .map(this::convertToOrderDetailResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy payment method của order theo order ID
     * Chỉ áp dụng cho DealerManager và DealerStaff thuộc dealer sở hữu order
     */
    @Transactional(readOnly = true)
    public OrderPaymentMethodResponse getOrderPaymentMethod(Integer orderId, String currentUserEmail) {
        // Validate input
        if (orderId == null) {
            throw new RuntimeException("Order ID is required");
        }

        // Lấy thông tin user hiện tại
        UserAccount currentUser = userAccountRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + currentUserEmail));

        String roleName = currentUser.getRoleId().getRoleName();

        // Kiểm tra role - chỉ cho phép DealerManager và DealerStaff
        if (!"DealerManager".equals(roleName) && !"DealerStaff".equals(roleName)) {
            throw new RuntimeException("Access denied. Only DealerManager and DealerStaff can access order payment method.");
        }

        // Kiểm tra user có thuộc dealer nào không
        if (currentUser.getDealer() == null) {
            throw new RuntimeException("User is not assigned to any dealer.");
        }

        Integer dealerId = currentUser.getDealer().getDealerId();

        // Tìm order và kiểm tra quyền truy cập
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        // Kiểm tra order có thuộc dealer hiện tại không
        if (!dealerId.equals(order.getDealer().getDealerId())) {
            throw new RuntimeException("Access denied. This order does not belong to your dealer.");
        }

        // Trả về payment method response
        return OrderPaymentMethodResponse.builder()
                .orderId(order.getOrderId())
                .paymentMethod(order.getPaymentMethod())
                .build();
    }

}
