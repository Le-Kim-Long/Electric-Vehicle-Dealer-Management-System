package org.example.repository;

import org.example.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Integer> {

    /**
     * Lấy tất cả orders của một dealer cụ thể
     */
    @Query("SELECT o FROM Orders o " +
           "WHERE o.dealer.dealerId = :dealerId " +
           "ORDER BY o.orderDate DESC")
    List<Orders> findAllByDealerId(@Param("dealerId") Integer dealerId);

    /**
     * Lấy tất cả orders của một dealer được tạo bởi user cụ thể
     */
    @Query("SELECT o FROM Orders o " +
           "WHERE o.dealer.dealerId = :dealerId " +
           "AND o.userAccount.userId = :userId " +
           "ORDER BY o.orderDate DESC")
    List<Orders> findAllByDealerIdAndUserId(@Param("dealerId") Integer dealerId, @Param("userId") Integer userId);

    /**
     * Lấy tất cả orders của một dealer được tạo bởi user có tên chứa keyword
     */
    @Query("SELECT o FROM Orders o " +
           "WHERE o.dealer.dealerId = :dealerId " +
           "AND LOWER(o.userAccount.username) LIKE LOWER(CONCAT('%', :creatorName, '%')) " +
           "ORDER BY o.orderDate DESC")
    List<Orders> findAllByDealerIdAndCreatorName(@Param("dealerId") Integer dealerId, @Param("creatorName") String creatorName);

    /**
     * Lấy order details của một order cụ thể
     */
    @Query("SELECT DISTINCT o FROM Orders o " +
           "LEFT JOIN FETCH o.orderDetails od " +
           "LEFT JOIN FETCH od.car c " +
           "LEFT JOIN FETCH c.carVariant cv " +
           "LEFT JOIN FETCH cv.carModel " +
           "WHERE o.orderId = :orderId")
    Orders findOrderWithDetails(@Param("orderId") Integer orderId);

    // ===== SALES REPORT QUERIES =====

    /**
     * Đếm tổng đơn hàng đã thanh toán theo completedDate cho dealer cụ thể
     */
    @Query(value = "SELECT COUNT(*) FROM ORDERS " +
            "WHERE Status = N'Đã thanh toán' " +
            "AND CompletionDate BETWEEN :startDate AND :endDate " +
            "AND DealerId = :dealerId",
            nativeQuery = true)
    Long countCompletedOrdersByCompletedDateRangeAndDealer(@Param("startDate") LocalDateTime startDate,
                                                          @Param("endDate") LocalDateTime endDate,
                                                          @Param("dealerId") Integer dealerId);

    /**
     * Đếm tổng đơn hàng chưa hoàn thành theo orderDate cho dealer cụ thể
     */
    @Query(value = "SELECT COUNT(*) FROM ORDERS " +
            "WHERE Status != N'Đã thanh toán' " +
            "AND OrderDate BETWEEN :startDate AND :endDate " +
            "AND DealerId = :dealerId",
            nativeQuery = true)
    Long countPendingOrdersByOrderDateRangeAndDealer(@Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate,
                                                    @Param("dealerId") Integer dealerId);

    /**
     * Đếm tổng xe bán được từ đơn hàng đã thanh toán cho dealer cụ thể
     */
    @Query(value = "SELECT ISNULL(SUM(od.Quantity), 0) FROM ORDERS o " +
            "INNER JOIN ORDER_DETAILS od ON o.OrderId = od.OrderId " +
            "WHERE o.Status = N'Đã thanh toán' " +
            "AND o.CompletionDate BETWEEN :startDate AND :endDate " +
            "AND o.DealerId = :dealerId",
            nativeQuery = true)
    Long countTotalCarsSoldByCompletedDateRangeAndDealer(@Param("startDate") LocalDateTime startDate,
                                                        @Param("endDate") LocalDateTime endDate,
                                                        @Param("dealerId") Integer dealerId);

    /**
     * Tính tổng doanh thu từ đơn hàng đã thanh toán (từ totalAmount) cho dealer cụ thể
     */
    @Query(value = "SELECT ISNULL(SUM(o.TotalAmount), 0) FROM ORDERS o " +
            "WHERE o.Status = N'Đã thanh toán' " +
            "AND o.CompletionDate BETWEEN :startDate AND :endDate " +
            "AND o.DealerId = :dealerId",
            nativeQuery = true)
    BigDecimal sumTotalRevenueByCompletedDateRangeAndDealer(@Param("startDate") LocalDateTime startDate,
                                                           @Param("endDate") LocalDateTime endDate,
                                                           @Param("dealerId") Integer dealerId);

    /**
     * Tính tổng lợi nhuận (unitPrice - manufacturerPrice) × quantity - discountAmount theo tỷ lệ từ đơn hàng đã thanh toán cho dealer cụ thể
     */
    @Query(value = "WITH OrderTotals AS (" +
            "SELECT o.OrderId, o.DiscountAmount, SUM(od2.Quantity) as TotalQuantity " +
            "FROM ORDERS o " +
            "INNER JOIN ORDER_DETAILS od2 ON o.OrderId = od2.OrderId " +
            "WHERE o.Status = N'Đã thanh toán' " +
            "AND o.CompletionDate BETWEEN :startDate AND :endDate " +
            "AND o.DealerId = :dealerId " +
            "GROUP BY o.OrderId, o.DiscountAmount" +
            ") " +
            "SELECT ISNULL(SUM(" +
            "(od.UnitPrice - c.Price) * od.Quantity - " +
            "(ot.DiscountAmount * od.Quantity / ot.TotalQuantity)" +
            "), 0) " +
            "FROM ORDERS o " +
            "INNER JOIN ORDER_DETAILS od ON o.OrderId = od.OrderId " +
            "INNER JOIN CAR c ON od.CarId = c.CarID " +
            "INNER JOIN OrderTotals ot ON o.OrderId = ot.OrderId " +
            "WHERE o.Status = N'Đã thanh toán' " +
            "AND o.CompletionDate BETWEEN :startDate AND :endDate " +
            "AND o.DealerId = :dealerId",
            nativeQuery = true)
    BigDecimal sumTotalProfitByCompletedDateRangeAndDealer(@Param("startDate") LocalDateTime startDate,
                                                          @Param("endDate") LocalDateTime endDate,
                                                          @Param("dealerId") Integer dealerId);

    // ===== DEALER-SPECIFIC REVENUE REPORT QUERIES =====

    /**
     * Lấy doanh thu theo từng ngày trong tháng cho dealer cụ thể
     */
    @Query(value = "SELECT DAY(o.CompletionDate) as periodNumber, " +
            "ISNULL(SUM(o.TotalAmount), 0) as revenue " +
            "FROM ORDERS o " +
            "WHERE o.Status = N'Đã thanh toán' " +
            "AND YEAR(o.CompletionDate) = :year " +
            "AND MONTH(o.CompletionDate) = :month " +
            "AND o.DealerId = :dealerId " +
            "GROUP BY DAY(o.CompletionDate) " +
            "ORDER BY DAY(o.CompletionDate)",
            nativeQuery = true)
    List<Object[]> getDailyRevenueByMonthForDealer(@Param("year") Integer year, @Param("month") Integer month, @Param("dealerId") Integer dealerId);

    /**
     * Lấy doanh thu theo từng tháng trong quý cho dealer cụ thể
     */
    @Query(value = "SELECT MONTH(o.CompletionDate) as periodNumber, " +
            "ISNULL(SUM(o.TotalAmount), 0) as revenue " +
            "FROM ORDERS o " +
            "WHERE o.Status = N'Đã thanh toán' " +
            "AND YEAR(o.CompletionDate) = :year " +
            "AND MONTH(o.CompletionDate) BETWEEN :startMonth AND :endMonth " +
            "AND o.DealerId = :dealerId " +
            "GROUP BY MONTH(o.CompletionDate) " +
            "ORDER BY MONTH(o.CompletionDate)",
            nativeQuery = true)
    List<Object[]> getMonthlyRevenueByQuarterForDealer(@Param("year") Integer year,
                                             @Param("startMonth") Integer startMonth,
                                             @Param("endMonth") Integer endMonth,
                                             @Param("dealerId") Integer dealerId);

    /**
     * Lấy doanh thu theo từng tháng trong năm cho dealer cụ thể
     */
    @Query(value = "SELECT MONTH(o.CompletionDate) as periodNumber, " +
            "ISNULL(SUM(o.TotalAmount), 0) as revenue " +
            "FROM ORDERS o " +
            "WHERE o.Status = N'Đã thanh toán' " +
            "AND YEAR(o.CompletionDate) = :year " +
            "AND o.DealerId = :dealerId " +
            "GROUP BY MONTH(o.CompletionDate) " +
            "ORDER BY MONTH(o.CompletionDate)",
            nativeQuery = true)
    List<Object[]> getMonthlyRevenueByYearForDealer(@Param("year") Integer year, @Param("dealerId") Integer dealerId);

    /**
     * Lấy tổng doanh thu của tháng cho dealer cụ thể
     */
    @Query(value = "SELECT ISNULL(SUM(o.TotalAmount), 0) " +
            "FROM ORDERS o " +
            "WHERE o.Status = N'Đã thanh toán' " +
            "AND YEAR(o.CompletionDate) = :year " +
            "AND MONTH(o.CompletionDate) = :month " +
            "AND o.DealerId = :dealerId",
            nativeQuery = true)
    BigDecimal getTotalRevenueByMonthForDealer(@Param("year") Integer year, @Param("month") Integer month, @Param("dealerId") Integer dealerId);

    /**
     * Lấy tổng doanh thu của quý cho dealer cụ thể
     */
    @Query(value = "SELECT ISNULL(SUM(o.TotalAmount), 0) " +
            "FROM ORDERS o " +
            "WHERE o.Status = N'Đã thanh toán' " +
            "AND YEAR(o.CompletionDate) = :year " +
            "AND MONTH(o.CompletionDate) BETWEEN :startMonth AND :endMonth " +
            "AND o.DealerId = :dealerId",
            nativeQuery = true)
    BigDecimal getTotalRevenueByQuarterForDealer(@Param("year") Integer year,
                                       @Param("startMonth") Integer startMonth,
                                       @Param("endMonth") Integer endMonth,
                                       @Param("dealerId") Integer dealerId);

    /**
     * Lấy tổng doanh thu của năm cho dealer cụ thể
     */
    @Query(value = "SELECT ISNULL(SUM(o.TotalAmount), 0) " +
            "FROM ORDERS o " +
            "WHERE o.Status = N'Đã thanh toán' " +
            "AND YEAR(o.CompletionDate) = :year " +
            "AND o.DealerId = :dealerId",
            nativeQuery = true)
    BigDecimal getTotalRevenueByYearForDealer(@Param("year") Integer year, @Param("dealerId") Integer dealerId);

    // ===== DEALER-SPECIFIC REVENUE BY MODEL QUERIES =====

    /**
     * Lấy doanh thu theo model name trong tháng cụ thể cho dealer cụ thể (từ finalPrice - khuyến mãi phân bổ)
     */
    @Query(value = "WITH OrderTotals AS (" +
            "    SELECT o.OrderId, " +
            "           ISNULL(p.Value, 0) as PromotionValue, " +
            "           SUM(od2.Quantity) as TotalQuantity " +
            "    FROM ORDERS o " +
            "    INNER JOIN ORDER_DETAILS od2 ON o.OrderId = od2.OrderId " +
            "    LEFT JOIN PROMOTION p ON o.PromotionId = p.PromotionId " +
            "    WHERE o.Status = N'Đã thanh toán' " +
            "    AND YEAR(o.CompletionDate) = :year " +
            "    AND MONTH(o.CompletionDate) = :month " +
            "    AND o.DealerId = :dealerId " +
            "    GROUP BY o.OrderId, p.Value" +
            ") " +
            "SELECT cm.ModelId, cm.ModelName, " +
            "ISNULL(SUM(od.Quantity), 0) as carsSold, " +
            "ISNULL(SUM(od.FinalPrice - (ot.PromotionValue * od.Quantity / ot.TotalQuantity)), 0) as revenue " +
            "FROM ORDERS o " +
            "INNER JOIN ORDER_DETAILS od ON o.OrderId = od.OrderId " +
            "INNER JOIN CAR c ON od.CarId = c.CarID " +
            "INNER JOIN CAR_VARIANT cv ON c.VariantID = cv.VariantID " +
            "INNER JOIN CAR_MODEL cm ON cv.ModelID = cm.ModelID " +
            "INNER JOIN OrderTotals ot ON o.OrderId = ot.OrderId " +
            "WHERE o.Status = N'Đã thanh toán' " +
            "AND YEAR(o.CompletionDate) = :year " +
            "AND MONTH(o.CompletionDate) = :month " +
            "AND o.DealerId = :dealerId " +
            "GROUP BY cm.ModelId, cm.ModelName " +
            "ORDER BY revenue DESC",
            nativeQuery = true)
    List<Object[]> getRevenueByModelByMonthForDealer(@Param("year") Integer year, @Param("month") Integer month, @Param("dealerId") Integer dealerId);

    /**
     * Lấy doanh thu theo model name trong quý cụ thể cho dealer cụ thể (từ finalPrice - khuyến mãi phân bổ)
     */
    @Query(value = "WITH OrderTotals AS (" +
            "    SELECT o.OrderId, " +
            "           ISNULL(p.Value, 0) as PromotionValue, " +
            "           SUM(od2.Quantity) as TotalQuantity " +
            "    FROM ORDERS o " +
            "    INNER JOIN ORDER_DETAILS od2 ON o.OrderId = od2.OrderId " +
            "    LEFT JOIN PROMOTION p ON o.PromotionId = p.PromotionId " +
            "    WHERE o.Status = N'Đã thanh toán' " +
            "    AND YEAR(o.CompletionDate) = :year " +
            "    AND MONTH(o.CompletionDate) BETWEEN :startMonth AND :endMonth " +
            "    AND o.DealerId = :dealerId " +
            "    GROUP BY o.OrderId, p.Value" +
            ") " +
            "SELECT cm.ModelId, cm.ModelName, " +
            "ISNULL(SUM(od.Quantity), 0) as carsSold, " +
            "ISNULL(SUM(od.FinalPrice - (ot.PromotionValue * od.Quantity / ot.TotalQuantity)), 0) as revenue " +
            "FROM ORDERS o " +
            "INNER JOIN ORDER_DETAILS od ON o.OrderId = od.OrderId " +
            "INNER JOIN CAR c ON od.CarId = c.CarID " +
            "INNER JOIN CAR_VARIANT cv ON c.VariantID = cv.VariantID " +
            "INNER JOIN CAR_MODEL cm ON cv.ModelID = cm.ModelID " +
            "INNER JOIN OrderTotals ot ON o.OrderId = ot.OrderId " +
            "WHERE o.Status = N'Đã thanh toán' " +
            "AND YEAR(o.CompletionDate) = :year " +
            "AND MONTH(o.CompletionDate) BETWEEN :startMonth AND :endMonth " +
            "AND o.DealerId = :dealerId " +
            "GROUP BY cm.ModelId, cm.ModelName " +
            "ORDER BY revenue DESC",
            nativeQuery = true)
    List<Object[]> getRevenueByModelByQuarterForDealer(@Param("year") Integer year,
                                             @Param("startMonth") Integer startMonth,
                                             @Param("endMonth") Integer endMonth,
                                             @Param("dealerId") Integer dealerId);

    /**
     * Lấy doanh thu theo model name trong năm cụ thể cho dealer cụ thể (từ finalPrice - khuyến mãi phân bổ)
     */
    @Query(value = "WITH OrderTotals AS (" +
            "    SELECT o.OrderId, " +
            "           ISNULL(p.Value, 0) as PromotionValue, " +
            "           SUM(od2.Quantity) as TotalQuantity " +
            "    FROM ORDERS o " +
            "    INNER JOIN ORDER_DETAILS od2 ON o.OrderId = od2.OrderId " +
            "    LEFT JOIN PROMOTION p ON o.PromotionId = p.PromotionId " +
            "    WHERE o.Status = N'Đã thanh toán' " +
            "    AND YEAR(o.CompletionDate) = :year " +
            "    AND o.DealerId = :dealerId " +
            "    GROUP BY o.OrderId, p.Value" +
            ") " +
            "SELECT cm.ModelId, cm.ModelName, " +
            "ISNULL(SUM(od.Quantity), 0) as carsSold, " +
            "ISNULL(SUM(od.FinalPrice - (ot.PromotionValue * od.Quantity / ot.TotalQuantity)), 0) as revenue " +
            "FROM ORDERS o " +
            "INNER JOIN ORDER_DETAILS od ON o.OrderId = od.OrderId " +
            "INNER JOIN CAR c ON od.CarId = c.CarID " +
            "INNER JOIN CAR_VARIANT cv ON c.VariantID = cv.VariantID " +
            "INNER JOIN CAR_MODEL cm ON cv.ModelID = cm.ModelID " +
            "INNER JOIN OrderTotals ot ON o.OrderId = ot.OrderId " +
            "WHERE o.Status = N'Đã thanh toán' " +
            "AND YEAR(o.CompletionDate) = :year " +
            "AND o.DealerId = :dealerId " +
            "GROUP BY cm.ModelId, cm.ModelName " +
            "ORDER BY revenue DESC",
            nativeQuery = true)
    List<Object[]> getRevenueByModelByYearForDealer(@Param("year") Integer year, @Param("dealerId") Integer dealerId);

    /**
     * Lấy tổng doanh thu từ finalPrice - khuyến mãi phân bổ trong tháng cho dealer cụ thể
     */
    @Query(value = "WITH OrderTotals AS (" +
            "    SELECT o.OrderId, " +
            "           ISNULL(p.Value, 0) as PromotionValue, " +
            "           SUM(od2.Quantity) as TotalQuantity " +
            "    FROM ORDERS o " +
            "    INNER JOIN ORDER_DETAILS od2 ON o.OrderId = od2.OrderId " +
            "    LEFT JOIN PROMOTION p ON o.PromotionId = p.PromotionId " +
            "    WHERE o.Status = N'Đã thanh toán' " +
            "    AND YEAR(o.CompletionDate) = :year " +
            "    AND MONTH(o.CompletionDate) = :month " +
            "    AND o.DealerId = :dealerId " +
            "    GROUP BY o.OrderId, p.Value" +
            ") " +
            "SELECT ISNULL(SUM(od.FinalPrice - (ot.PromotionValue * od.Quantity / ot.TotalQuantity)), 0) " +
            "FROM ORDERS o " +
            "INNER JOIN ORDER_DETAILS od ON o.OrderId = od.OrderId " +
            "INNER JOIN OrderTotals ot ON o.OrderId = ot.OrderId " +
            "WHERE o.Status = N'Đã thanh toán' " +
            "AND YEAR(o.CompletionDate) = :year " +
            "AND MONTH(o.CompletionDate) = :month " +
            "AND o.DealerId = :dealerId",
            nativeQuery = true)
    BigDecimal getTotalRevenueFromFinalPriceByMonthForDealer(@Param("year") Integer year, @Param("month") Integer month, @Param("dealerId") Integer dealerId);

    /**
     * Lấy tổng doanh thu từ finalPrice - khuyến mãi phân bổ trong quý cho dealer cụ thể
     */
    @Query(value = "WITH OrderTotals AS (" +
            "    SELECT o.OrderId, " +
            "           ISNULL(p.Value, 0) as PromotionValue, " +
            "           SUM(od2.Quantity) as TotalQuantity " +
            "    FROM ORDERS o " +
            "    INNER JOIN ORDER_DETAILS od2 ON o.OrderId = od2.OrderId " +
            "    LEFT JOIN PROMOTION p ON o.PromotionId = p.PromotionId " +
            "    WHERE o.Status = N'Đã thanh toán' " +
            "    AND YEAR(o.CompletionDate) = :year " +
            "    AND MONTH(o.CompletionDate) BETWEEN :startMonth AND :endMonth " +
            "    AND o.DealerId = :dealerId " +
            "    GROUP BY o.OrderId, p.Value" +
            ") " +
            "SELECT ISNULL(SUM(od.FinalPrice - (ot.PromotionValue * od.Quantity / ot.TotalQuantity)), 0) " +
            "FROM ORDERS o " +
            "INNER JOIN ORDER_DETAILS od ON o.OrderId = od.OrderId " +
            "INNER JOIN OrderTotals ot ON o.OrderId = ot.OrderId " +
            "WHERE o.Status = N'Đã thanh toán' " +
            "AND YEAR(o.CompletionDate) = :year " +
            "AND MONTH(o.CompletionDate) BETWEEN :startMonth AND :endMonth " +
            "AND o.DealerId = :dealerId",
            nativeQuery = true)
    BigDecimal getTotalRevenueFromFinalPriceByQuarterForDealer(@Param("year") Integer year,
                                                     @Param("startMonth") Integer startMonth,
                                                     @Param("endMonth") Integer endMonth,
                                                     @Param("dealerId") Integer dealerId);

    /**
     * Lấy tổng doanh thu từ finalPrice - khuyến mãi phân bổ trong năm cho dealer cụ thể
     */
    @Query(value = "WITH OrderTotals AS (" +
            "    SELECT o.OrderId, " +
            "           ISNULL(p.Value, 0) as PromotionValue, " +
            "           SUM(od2.Quantity) as TotalQuantity " +
            "    FROM ORDERS o " +
            "    INNER JOIN ORDER_DETAILS od2 ON o.OrderId = od2.OrderId " +
            "    LEFT JOIN PROMOTION p ON o.PromotionId = p.PromotionId " +
            "    WHERE o.Status = N'Đã thanh toán' " +
            "    AND YEAR(o.CompletionDate) = :year " +
            "    AND o.DealerId = :dealerId " +
            "    GROUP BY o.OrderId, p.Value" +
            ") " +
            "SELECT ISNULL(SUM(od.FinalPrice - (ot.PromotionValue * od.Quantity / ot.TotalQuantity)), 0) " +
            "FROM ORDERS o " +
            "INNER JOIN ORDER_DETAILS od ON o.OrderId = od.OrderId " +
            "INNER JOIN OrderTotals ot ON o.OrderId = ot.OrderId " +
            "WHERE o.Status = N'Đã thanh toán' " +
            "AND YEAR(o.CompletionDate) = :year " +
            "AND o.DealerId = :dealerId",
            nativeQuery = true)
    BigDecimal getTotalRevenueFromFinalPriceByYearForDealer(@Param("year") Integer year, @Param("dealerId") Integer dealerId);

    // ===== DEALER-SPECIFIC REVENUE BY STAFF QUERIES =====

    /**
     * Lấy doanh thu theo staff trong tháng cụ thể cho dealer cụ thể (từ totalAmount)
     */
    @Query(value = "SELECT ua.UserId, ua.FullName, " +
            "ISNULL(SUM(od.Quantity), 0) as carsSold, " +
            "ISNULL(SUM(DISTINCT o.TotalAmount), 0) as revenue " +
            "FROM ORDERS o " +
            "INNER JOIN ORDER_DETAILS od ON o.OrderId = od.OrderId " +
            "INNER JOIN USER_ACCOUNT ua ON o.UserId = ua.UserId " +
            "INNER JOIN ROLE r ON ua.RoleID = r.RoleID " +
            "WHERE o.Status = N'Đã thanh toán' " +
            "AND r.RoleName IN ('DealerStaff') " +
            "AND o.DealerId = :dealerId " +
            "AND YEAR(o.CompletionDate) = :year " +
            "AND MONTH(o.CompletionDate) = :month " +
            "GROUP BY ua.UserId, ua.FullName " +
            "ORDER BY revenue DESC",
            nativeQuery = true)
    List<Object[]> getRevenueByStaffByMonthForDealer(@Param("year") Integer year, @Param("month") Integer month, @Param("dealerId") Integer dealerId);

    /**
     * Lấy doanh thu theo staff trong quý cụ thể cho dealer cụ thể (từ totalAmount)
     */
    @Query(value = "SELECT ua.UserId, ua.FullName, " +
            "ISNULL(SUM(od.Quantity), 0) as carsSold, " +
            "ISNULL(SUM(DISTINCT o.TotalAmount), 0) as revenue " +
            "FROM ORDERS o " +
            "INNER JOIN ORDER_DETAILS od ON o.OrderId = od.OrderId " +
            "INNER JOIN USER_ACCOUNT ua ON o.UserId = ua.UserId " +
            "INNER JOIN ROLE r ON ua.RoleID = r.RoleID " +
            "WHERE o.Status = N'Đã thanh toán' " +
            "AND r.RoleName IN ('DealerStaff') " +
            "AND o.DealerId = :dealerId " +
            "AND YEAR(o.CompletionDate) = :year " +
            "AND MONTH(o.CompletionDate) BETWEEN :startMonth AND :endMonth " +
            "GROUP BY ua.UserId, ua.FullName " +
            "ORDER BY revenue DESC",
            nativeQuery = true)
    List<Object[]> getRevenueByStaffByQuarterForDealer(@Param("year") Integer year,
                                             @Param("startMonth") Integer startMonth,
                                             @Param("endMonth") Integer endMonth,
                                             @Param("dealerId") Integer dealerId);

    /**
     * Lấy doanh thu theo staff trong năm cụ thể cho dealer cụ thể (từ totalAmount)
     */
    @Query(value = "SELECT ua.UserId, ua.FullName, " +
            "ISNULL(SUM(od.Quantity), 0) as carsSold, " +
            "ISNULL(SUM(DISTINCT o.TotalAmount), 0) as revenue " +
            "FROM ORDERS o " +
            "INNER JOIN ORDER_DETAILS od ON o.OrderId = od.OrderId " +
            "INNER JOIN USER_ACCOUNT ua ON o.UserId = ua.UserId " +
            "INNER JOIN ROLE r ON ua.RoleID = r.RoleID " +
            "WHERE o.Status = N'Đã thanh toán' " +
            "AND r.RoleName IN ('DealerStaff') " +
            "AND o.DealerId = :dealerId " +
            "AND YEAR(o.CompletionDate) = :year " +
            "GROUP BY ua.UserId, ua.FullName " +
            "ORDER BY revenue DESC",
            nativeQuery = true)
    List<Object[]> getRevenueByStaffByYearForDealer(@Param("year") Integer year, @Param("dealerId") Integer dealerId);

    // ===== REVENUE BY MODEL QUERIES =====

    /**
     * Lấy tất cả model của đại lý
     */
    @Query(value = "SELECT DISTINCT cm.ModelId, cm.ModelName " +
            "FROM CAR_MODEL cm " +
            "INNER JOIN CAR_VARIANT cv ON cm.ModelID = cv.ModelID " +
            "INNER JOIN CAR c ON cv.VariantID = c.VariantID " +
            "INNER JOIN DEALER_CAR dc ON c.CarID = dc.CarID " +
            "WHERE dc.DealerID = :dealerId " +
            "ORDER BY cm.ModelName",
            nativeQuery = true)
    List<Object[]> getAllModelsByDealer(@Param("dealerId") Integer dealerId);

    /**
     * Lấy tất cả staff của đại lý
     */
    @Query(value = "SELECT ua.UserId, ua.FullName " +
            "FROM USER_ACCOUNT ua " +
            "INNER JOIN ROLE r ON ua.RoleID = r.RoleID " +
            "WHERE ua.DealerID = :dealerId " +
            "AND r.RoleName IN ('DealerStaff') " +
            "ORDER BY ua.FullName",
            nativeQuery = true)
    List<Object[]> getAllStaffByDealer(@Param("dealerId") Integer dealerId);

    // ===== TOTAL REVENUE FOR STAFF REPORT QUERIES =====

    /**
     * Lấy tổng doanh thu từ finalPrice trong tháng cho dealer cụ thể (để tính tổng cho staff report)
     */
    @Query(value = "WITH OrderTotals AS (" +
            "    SELECT o.OrderId, " +
            "           ISNULL(p.Value, 0) as PromotionValue, " +
            "           SUM(od2.Quantity) as TotalQuantity " +
            "    FROM ORDERS o " +
            "    INNER JOIN ORDER_DETAILS od2 ON o.OrderId = od2.OrderId " +
            "    LEFT JOIN PROMOTION p ON o.PromotionId = p.PromotionId " +
            "    WHERE o.Status = N'Đã thanh toán' " +
            "    AND YEAR(o.CompletionDate) = :year " +
            "    AND MONTH(o.CompletionDate) = :month " +
            "    AND o.DealerId = :dealerId " +
            "    GROUP BY o.OrderId, p.Value" +
            ") " +
            "SELECT ISNULL(SUM(od.FinalPrice - (ot.PromotionValue * od.Quantity / ot.TotalQuantity)), 0) " +
            "FROM ORDERS o " +
            "INNER JOIN ORDER_DETAILS od ON o.OrderId = od.OrderId " +
            "INNER JOIN OrderTotals ot ON o.OrderId = ot.OrderId " +
            "WHERE o.Status = N'Đã thanh toán' " +
            "AND YEAR(o.CompletionDate) = :year " +
            "AND MONTH(o.CompletionDate) = :month " +
            "AND o.DealerId = :dealerId",
            nativeQuery = true)
    BigDecimal getTotalRevenueForStaffReportByMonthForDealer(@Param("year") Integer year, @Param("month") Integer month, @Param("dealerId") Integer dealerId);

    /**
     * Lấy tổng doanh thu từ finalPrice trong quý cho dealer cụ thể (để tính tổng cho staff report)
     */
    @Query(value = "WITH OrderTotals AS (" +
            "    SELECT o.OrderId, " +
            "           ISNULL(p.Value, 0) as PromotionValue, " +
            "           SUM(od2.Quantity) as TotalQuantity " +
            "    FROM ORDERS o " +
            "    INNER JOIN ORDER_DETAILS od2 ON o.OrderId = od2.OrderId " +
            "    LEFT JOIN PROMOTION p ON o.PromotionId = p.PromotionId " +
            "    WHERE o.Status = N'Đã thanh toán' " +
            "    AND YEAR(o.CompletionDate) = :year " +
            "    AND MONTH(o.CompletionDate) BETWEEN :startMonth AND :endMonth " +
            "    AND o.DealerId = :dealerId " +
            "    GROUP BY o.OrderId, p.Value" +
            ") " +
            "SELECT ISNULL(SUM(od.FinalPrice - (ot.PromotionValue * od.Quantity / ot.TotalQuantity)), 0) " +
            "FROM ORDERS o " +
            "INNER JOIN ORDER_DETAILS od ON o.OrderId = od.OrderId " +
            "INNER JOIN OrderTotals ot ON o.OrderId = ot.OrderId " +
            "WHERE o.Status = N'Đã thanh toán' " +
            "AND YEAR(o.CompletionDate) = :year " +
            "AND MONTH(o.CompletionDate) BETWEEN :startMonth AND :endMonth " +
            "AND o.DealerId = :dealerId",
            nativeQuery = true)
    BigDecimal getTotalRevenueForStaffReportByQuarterForDealer(@Param("year") Integer year,
                                                     @Param("startMonth") Integer startMonth,
                                                     @Param("endMonth") Integer endMonth,
                                                     @Param("dealerId") Integer dealerId);

    /**
     * Lấy tổng doanh thu từ finalPrice trong năm cho dealer cụ thể (để tính tổng cho staff report)
     */
    @Query(value = "WITH OrderTotals AS (" +
            "    SELECT o.OrderId, " +
            "           ISNULL(p.Value, 0) as PromotionValue, " +
            "           SUM(od2.Quantity) as TotalQuantity " +
            "    FROM ORDERS o " +
            "    INNER JOIN ORDER_DETAILS od2 ON o.OrderId = od2.OrderId " +
            "    LEFT JOIN PROMOTION p ON o.PromotionId = p.PromotionId " +
            "    WHERE o.Status = N'Đã thanh toán' " +
            "    AND YEAR(o.CompletionDate) = :year " +
            "    AND o.DealerId = :dealerId " +
            "    GROUP BY o.OrderId, p.Value" +
            ") " +
            "SELECT ISNULL(SUM(od.FinalPrice - (ot.PromotionValue * od.Quantity / ot.TotalQuantity)), 0) " +
            "FROM ORDERS o " +
            "INNER JOIN ORDER_DETAILS od ON o.OrderId = od.OrderId " +
            "INNER JOIN OrderTotals ot ON o.OrderId = ot.OrderId " +
            "WHERE o.Status = N'Đã thanh toán' " +
            "AND YEAR(o.CompletionDate) = :year " +
            "AND o.DealerId = :dealerId",
            nativeQuery = true)
    BigDecimal getTotalRevenueForStaffReportByYearForDealer(@Param("year") Integer year, @Param("dealerId") Integer dealerId);
}
