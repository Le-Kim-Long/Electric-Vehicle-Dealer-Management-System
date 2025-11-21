package org.example.service;

import org.example.dto.*;

import java.util.List;

public interface OrdersService {

    /**
     * Lấy tất cả orders của dealer mà user đang đăng nhập thuộc về
     * DealerManager: Lấy tất cả orders của dealer
     * DealerStaff: Chỉ lấy orders được tạo bởi user đó
     */
    List<DealerOrderResponse> getOrdersByCurrentUserDealer(String currentUserEmail);

    /**
     * Lấy orders theo tên người tạo cho DealerManager
     * Chỉ DealerManager mới được phép sử dụng API này
     */
    List<DealerOrderResponse> getOrdersByCreatorName(String creatorName, String currentUserEmail);

    /**
     * Tạo bản nháp order cho dealer manager/staff
     */
    CreateDraftOrderResponse createDraftOrder(CreateDraftOrderRequest request, String currentUserEmail);

    /**
     * Tạo order detail với unit price tự động từ dealer price
     */
    CreateOrderDetailResponse createOrderDetail(CreateOrderDetailRequest request, String currentUserEmail);

    /**
     * Cập nhật subTotal trong bảng Orders sau khi thêm order detail
     */
    void updateOrderSubTotal(Integer orderId);

    /**
     * Xóa order detail và hoàn trả số lượng xe cho đại lý
     */
    void deleteOrderDetail(Integer orderDetailId, String currentUserEmail);

    /**
     * Cập nhật order detail với logic tính toán tồn kho và subTotal
     */
    UpdateOrderDetailResponse updateOrderDetail(Integer orderDetailId, UpdateOrderDetailRequest request, String currentUserEmail);

    /**
     * Cập nhật promotion cho order với logic tính toán discount và total amount
     */
    UpdateOrderPromotionResponse updateOrderPromotion(Integer orderId, UpdateOrderPromotionRequest request, String currentUserEmail);

    /**
     * Cập nhật payment method cho order
     */
    UpdateOrderPaymentMethodResponse updateOrderPaymentMethod(Integer orderId, UpdateOrderPaymentMethodRequest request, String currentUserEmail);

    /**
     * Cập nhật status cho order
     */
    UpdateOrderStatusResponse updateOrderStatus(Integer orderId, UpdateOrderStatusRequest request, String currentUserEmail);

    /**
     * Lấy thông tin đơn hàng theo order ID
     * Chỉ áp dụng cho DealerManager và DealerStaff thuộc dealer sở hữu order
     */
    DealerOrderResponse getOrderById(Integer orderId, String currentUserEmail);

    /**
     * Lấy danh sách order details theo order ID
     * Chỉ áp dụng cho DealerManager và DealerStaff thuộc dealer sở hữu order
     */
    List<OrderDetailResponse> getOrderDetailsByOrderId(Integer orderId, String currentUserEmail);

    /**
     * Lấy payment method của order theo order ID
     * Chỉ áp dụng cho DealerManager và DealerStaff thuộc dealer sở hữu order
     */
    OrderPaymentMethodResponse getOrderPaymentMethod(Integer orderId, String currentUserEmail);
}
