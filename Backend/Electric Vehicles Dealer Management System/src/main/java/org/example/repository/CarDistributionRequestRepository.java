package org.example.repository;

import org.example.entity.CarDistributionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CarDistributionRequestRepository extends JpaRepository<CarDistributionRequest, Integer> {

    // Find requests by dealer ID
    @Query("SELECT cdr FROM CarDistributionRequest cdr WHERE cdr.dealer.dealerId = :dealerId ORDER BY cdr.requestDate DESC")
    List<CarDistributionRequest> findByDealerId(@Param("dealerId") Integer dealerId);

    // Find requests by status
    @Query("SELECT cdr FROM CarDistributionRequest cdr WHERE cdr.status = :status ORDER BY cdr.requestDate DESC")
    List<CarDistributionRequest> findByStatus(@Param("status") String status);

    // Find requests by dealer ID and status
    @Query("SELECT cdr FROM CarDistributionRequest cdr WHERE cdr.dealer.dealerId = :dealerId AND cdr.status = :status ORDER BY cdr.requestDate DESC")
    List<CarDistributionRequest> findByDealerIdAndStatus(@Param("dealerId") Integer dealerId, @Param("status") String status);

    // Tính tổng số xe nhập vào từ các yêu cầu phân phối đã giao trong khoảng thời gian cho dealer cụ thể
    @Query(value = "SELECT COALESCE(SUM(cdr.Quantity), 0) FROM CAR_DISTRIBUTION_REQUEST cdr " +
                   "WHERE cdr.Status = N'Đã giao' " +
                   "AND cdr.ActualDeliveryDate BETWEEN :startDate AND :endDate " +
                   "AND cdr.DealerId = :dealerId",
           nativeQuery = true)
    Long sumTotalCarsDistributedByDeliveryDateRangeAndDealer(@Param("startDate") java.time.LocalDateTime startDate,
                                                            @Param("endDate") java.time.LocalDateTime endDate,
                                                            @Param("dealerId") Integer dealerId);


    // Lấy chi phí nhập xe theo ngày trong tháng cho dealer cụ thể
    @Query(value = "SELECT DAY(cdr.ActualDeliveryDate) as day, " +
                   "COALESCE(SUM(cdr.TotalAmount), 0) as totalCost, " +
                   "COALESCE(SUM(cdr.Quantity), 0) as totalQuantity " +
                   "FROM CAR_DISTRIBUTION_REQUEST cdr " +
                   "WHERE cdr.Status = N'Đã giao' " +
                   "AND cdr.DealerID = :dealerId " +
                   "AND YEAR(cdr.ActualDeliveryDate) = :year " +
                   "AND MONTH(cdr.ActualDeliveryDate) = :month " +
                   "GROUP BY DAY(cdr.ActualDeliveryDate) " +
                   "ORDER BY DAY(cdr.ActualDeliveryDate)",
           nativeQuery = true)
    List<Object[]> findDailyImportCostInMonthForDealer(@Param("year") Integer year,
                                                       @Param("month") Integer month,
                                                       @Param("dealerId") Integer dealerId);

    // Lấy chi phí nhập xe theo tháng trong quý cho dealer cụ thể
    @Query(value = "SELECT MONTH(cdr.ActualDeliveryDate) as month, " +
                   "COALESCE(SUM(cdr.TotalAmount), 0) as totalCost, " +
                   "COALESCE(SUM(cdr.Quantity), 0) as totalQuantity " +
                   "FROM CAR_DISTRIBUTION_REQUEST cdr " +
                   "WHERE cdr.Status = N'Đã giao' " +
                   "AND cdr.DealerID = :dealerId " +
                   "AND YEAR(cdr.ActualDeliveryDate) = :year " +
                   "AND MONTH(cdr.ActualDeliveryDate) BETWEEN :startMonth AND :endMonth " +
                   "GROUP BY MONTH(cdr.ActualDeliveryDate) " +
                   "ORDER BY MONTH(cdr.ActualDeliveryDate)",
           nativeQuery = true)
    List<Object[]> findMonthlyImportCostInQuarterForDealer(@Param("year") Integer year,
                                                           @Param("startMonth") Integer startMonth,
                                                           @Param("endMonth") Integer endMonth,
                                                           @Param("dealerId") Integer dealerId);

    // Lấy chi phí nhập xe theo tháng trong năm cho dealer cụ thể
    @Query(value = "SELECT MONTH(cdr.ActualDeliveryDate) as month, " +
                   "COALESCE(SUM(cdr.TotalAmount), 0) as totalCost, " +
                   "COALESCE(SUM(cdr.Quantity), 0) as totalQuantity " +
                   "FROM CAR_DISTRIBUTION_REQUEST cdr " +
                   "WHERE cdr.Status = N'Đã giao' " +
                   "AND cdr.DealerID = :dealerId " +
                   "AND YEAR(cdr.ActualDeliveryDate) = :year " +
                   "GROUP BY MONTH(cdr.ActualDeliveryDate) " +
                   "ORDER BY MONTH(cdr.ActualDeliveryDate)",
           nativeQuery = true)
    List<Object[]> findMonthlyImportCostInYearForDealer(@Param("year") Integer year,
                                                        @Param("dealerId") Integer dealerId);
}
