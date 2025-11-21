package org.example.service;

import org.example.dto.*;

import java.util.List;

public interface PaymentService {

    /**
     * Tạo hóa đơn thanh toán mới cho "Trả thẳng"
     */
    CreatePaymentResponse createPayment(CreatePaymentRequest request, String email);

    /**
     * Cập nhật payment method và reset status về "Chờ xử lý"
     */
    UpdatePaymentMethodResponse updatePaymentMethod(Integer paymentId, UpdatePaymentMethodRequest request, String email);

    /**
     * Cập nhật trạng thái thanh toán và tự động cập nhật trạng thái đơn hàng nếu cần
     */
    UpdatePaymentStatusResponse updatePaymentStatus(Integer paymentId, UpdatePaymentStatusRequest request, String email);

    /**
     * Xóa hóa đơn thanh toán
     */
    void deletePayment(Integer paymentId, String email);

    /**
     * Lấy tất cả payments của một order với kiểm tra quyền truy cập
     */
    List<PaymentResponse> getPaymentsByOrderId(Integer orderId, String email);
}
