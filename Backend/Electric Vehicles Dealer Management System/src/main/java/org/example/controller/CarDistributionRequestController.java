package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.dto.CreateDistributionRequestDto;
import org.example.dto.DistributionRequestResponseDto;
import org.example.dto.SetDeliveryDateDto;
import org.example.entity.UserAccount;
import org.example.repository.UserAccountRepository;
import org.example.service.CarDistributionRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/distribution-requests")
@Tag(name = "Car Distribution Request", description = "APIs for managing car distribution requests from dealers")
@SecurityRequirement(name = "Bearer Authentication")
public class CarDistributionRequestController {

    @Autowired
    private CarDistributionRequestService distributionRequestService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @PostMapping("/create")
    @Operation(summary = "Tạo yêu cầu phân phối xe",
               description = "Dealer Manager tạo yêu cầu phân phối xe từ hãng xe. Có thể tạo yêu cầu cho xe mới hoặc bổ sung số lượng xe đã có tại đại lý.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Tạo yêu cầu thành công"),
        @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ"),
        @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
        @ApiResponse(responseCode = "403", description = "Không có quyền truy cập"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy xe hoặc dealer")
    })
    public ResponseEntity<?> createDistributionRequest(@Valid @RequestBody CreateDistributionRequestDto requestDto) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Authorization header is required. Please login first to get JWT token.");
        }

        String email = authentication.getName();

        if (email == null || email.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Invalid authentication. Please login again.");
        }

        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        String roleName = user.getRoleId().getRoleName();

        if (!"DealerManager".equals(roleName)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Access denied. Only Dealer Manager can create distribution requests.");
        }

        try {
            DistributionRequestResponseDto response = distributionRequestService.createDistributionRequest(email, requestDto);

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("message", "Yêu cầu phân phối xe đã được tạo thành công");
            responseMap.put("data", response);

            return ResponseEntity.status(HttpStatus.CREATED).body(responseMap);

        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();

            if (e.getMessage().contains("Car not found")) {
                errorResponse.put("error", "Không tìm thấy xe với thông tin đã cung cấp");
                errorResponse.put("message", e.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            if (e.getMessage().contains("User not found") || e.getMessage().contains("not associated")) {
                errorResponse.put("error", "Thông tin người dùng hoặc dealer không hợp lệ");
                errorResponse.put("message", e.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            errorResponse.put("error", "Có lỗi xảy ra khi tạo yêu cầu");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/my-requests")
    @Operation(summary = "Lấy danh sách yêu cầu của dealer hiện tại",
               description = "Lấy tất cả yêu cầu phân phối xe của dealer hiện tại")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
        @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
        @ApiResponse(responseCode = "403", description = "Không có quyền truy cập"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy dealer")
    })
    public ResponseEntity<?> getMyDistributionRequests() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Authorization header is required. Please login first to get JWT token.");
        }

        String email = authentication.getName();

        if (email == null || email.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Invalid authentication. Please login again.");
        }

        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        String roleName = user.getRoleId().getRoleName();

        if (!"DealerManager".equals(roleName) && !"DealerStaff".equals(roleName)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Access denied. Only Dealer Manager and Dealer Staff can view dealer requests.");
        }

        try {
            List<DistributionRequestResponseDto> requests = distributionRequestService.getDistributionRequestsByDealer(email);

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("message", "Lấy danh sách yêu cầu thành công");
            responseMap.put("data", requests);
            responseMap.put("totalCount", requests.size());

            return ResponseEntity.ok(responseMap);

        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Có lỗi xảy ra khi lấy danh sách yêu cầu");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/my-requests/status/{status}")
    @Operation(summary = "Lấy yêu cầu theo trạng thái của dealer hiện tại",
               description = "Lấy yêu cầu phân phối xe theo trạng thái của dealer hiện tại")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
        @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
        @ApiResponse(responseCode = "403", description = "Không có quyền truy cập"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy dealer")
    })
    public ResponseEntity<?> getMyDistributionRequestsByStatus(@PathVariable String status) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Authorization header is required. Please login first to get JWT token.");
        }

        String email = authentication.getName();

        if (email == null || email.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Invalid authentication. Please login again.");
        }

        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        String roleName = user.getRoleId().getRoleName();

        if (!"DealerManager".equals(roleName) && !"DealerStaff".equals(roleName)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Access denied. Only Dealer Manager and Dealer Staff can view dealer requests.");
        }

        try {
            List<DistributionRequestResponseDto> requests = distributionRequestService.getDistributionRequestsByDealerAndStatus(email, status);

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("message", "Lấy danh sách yêu cầu theo trạng thái thành công");
            responseMap.put("data", requests);
            responseMap.put("status", status);
            responseMap.put("totalCount", requests.size());

            return ResponseEntity.ok(responseMap);

        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Có lỗi xảy ra khi lấy danh sách yêu cầu");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/all")
    @Operation(summary = "Lấy tất cả yêu cầu phân phối trong hệ thống",
               description = "Admin/EVM Staff lấy tất cả yêu cầu phân phối xe trong hệ thống")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
        @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
        @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    public ResponseEntity<?> getAllDistributionRequests() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Authorization header is required. Please login first to get JWT token.");
        }

        String email = authentication.getName();

        if (email == null || email.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Invalid authentication. Please login again.");
        }

        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        String roleName = user.getRoleId().getRoleName();

        if (!"Admin".equals(roleName) && !"EVMStaff".equals(roleName)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Access denied. Only Admin and EVM Staff can view all distribution requests.");
        }

        try {
            List<DistributionRequestResponseDto> requests = distributionRequestService.getAllDistributionRequests();

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("message", "Lấy tất cả yêu cầu phân phối thành công");
            responseMap.put("data", requests);
            responseMap.put("totalCount", requests.size());

            return ResponseEntity.ok(responseMap);

        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Có lỗi xảy ra khi lấy danh sách yêu cầu");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/all/status/{status}")
    @Operation(summary = "Lấy yêu cầu theo trạng thái trong hệ thống",
               description = "Admin/EVM Staff lấy yêu cầu phân phối xe theo trạng thái trong hệ thống")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
        @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
        @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    public ResponseEntity<?> getAllDistributionRequestsByStatus(@PathVariable String status) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Authorization header is required. Please login first to get JWT token.");
        }

        String email = authentication.getName();

        if (email == null || email.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Invalid authentication. Please login again.");
        }

        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        String roleName = user.getRoleId().getRoleName();

        if (!"Admin".equals(roleName) && !"EVMStaff".equals(roleName)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Access denied. Only Admin and EVM Staff can view distribution requests by status.");
        }

        try {
            List<DistributionRequestResponseDto> requests = distributionRequestService.getDistributionRequestsByStatus(status);

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("message", "Lấy yêu cầu theo trạng thái thành công");
            responseMap.put("data", requests);
            responseMap.put("status", status);
            responseMap.put("totalCount", requests.size());

            return ResponseEntity.ok(responseMap);

        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Có lỗi xảy ra khi lấy danh sách yêu cầu");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/{requestId}/approve")
    @Operation(summary = "Duyệt yêu cầu phân phối xe",
               description = "Admin/EVM Staff duyệt yêu cầu phân phối xe từ dealer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Duyệt yêu cầu thành công"),
        @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
        @ApiResponse(responseCode = "403", description = "Không có quyền truy cập"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy yêu cầu"),
        @ApiResponse(responseCode = "400", description = "Yêu cầu không thể duyệt")
    })
    public ResponseEntity<?> approveDistributionRequest(@PathVariable Integer requestId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Authorization header is required. Please login first to get JWT token.");
        }

        String email = authentication.getName();

        if (email == null || email.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Invalid authentication. Please login again.");
        }

        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        String roleName = user.getRoleId().getRoleName();

        if (!"Admin".equals(roleName) && !"EVMStaff".equals(roleName)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Access denied. Only Admin and EVM Staff can approve distribution requests.");
        }

        try {
            DistributionRequestResponseDto response = distributionRequestService.approveDistributionRequest(requestId, email);

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("message", "Yêu cầu phân phối xe đã được duyệt thành công");
            responseMap.put("data", response);

            return ResponseEntity.ok(responseMap);

        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();

            if (e.getMessage().contains("not found")) {
                errorResponse.put("error", "Không tìm thấy yêu cầu phân phối");
                errorResponse.put("message", e.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            if (e.getMessage().contains("Can only approve")) {
                errorResponse.put("error", "Không thể duyệt yêu cầu này");
                errorResponse.put("message", e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            errorResponse.put("error", "Có lỗi xảy ra khi duyệt yêu cầu");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/{requestId}/reject")
    @Operation(summary = "Từ chối yêu cầu phân phối xe",
               description = "Admin/EVM Staff từ chối yêu cầu phân phối xe từ dealer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Từ chối yêu cầu thành công"),
        @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
        @ApiResponse(responseCode = "403", description = "Không có quyền truy cập"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy yêu cầu"),
        @ApiResponse(responseCode = "400", description = "Yêu cầu không thể từ chối")
    })
    public ResponseEntity<?> rejectDistributionRequest(@PathVariable Integer requestId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Authorization header is required. Please login first to get JWT token.");
        }

        String email = authentication.getName();

        if (email == null || email.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Invalid authentication. Please login again.");
        }

        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        String roleName = user.getRoleId().getRoleName();

        if (!"Admin".equals(roleName) && !"EVMStaff".equals(roleName)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Access denied. Only Admin and EVM Staff can reject distribution requests.");
        }

        try {
            DistributionRequestResponseDto response = distributionRequestService.rejectDistributionRequest(requestId, email);

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("message", "Yêu cầu phân phối xe đã được từ chối");
            responseMap.put("data", response);

            return ResponseEntity.ok(responseMap);

        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();

            if (e.getMessage().contains("not found")) {
                errorResponse.put("error", "Không tìm thấy yêu cầu phân phối");
                errorResponse.put("message", e.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            if (e.getMessage().contains("Can only reject")) {
                errorResponse.put("error", "Không thể từ chối yêu cầu này");
                errorResponse.put("message", e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            errorResponse.put("error", "Có lỗi xảy ra khi từ chối yêu cầu");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/{requestId}/set-delivery")
    @Operation(summary = "Set ngày giao xe và bắt đầu giao xe",
               description = "EVM Staff set ngày giao xe dự kiến và chuyển trạng thái thành 'Đang giao'")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Set ngày giao xe thành công"),
        @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
        @ApiResponse(responseCode = "403", description = "Không có quyền truy cập"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy yêu cầu"),
        @ApiResponse(responseCode = "400", description = "Yêu cầu không thể set ngày giao xe")
    })
    public ResponseEntity<?> setDeliveryDateAndStartDelivery(
            @PathVariable Integer requestId,
            @Valid @RequestBody SetDeliveryDateDto deliveryDateDto) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Authorization header is required. Please login first to get JWT token.");
        }

        String email = authentication.getName();

        if (email == null || email.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Invalid authentication. Please login again.");
        }

        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        String roleName = user.getRoleId().getRoleName();

        if (!"EVMStaff".equals(roleName)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Access denied. Only EVM Staff can set delivery date and start delivery.");
        }

        try {
            DistributionRequestResponseDto response = distributionRequestService.setDeliveryDateAndStartDelivery(
                    requestId, email, deliveryDateDto.getExpectedDeliveryDate());

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("message", "Ngày giao xe đã được thiết lập và bắt đầu giao xe thành công");
            responseMap.put("data", response);

            return ResponseEntity.ok(responseMap);

        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();

            if (e.getMessage().contains("not found")) {
                errorResponse.put("error", "Không tìm thấy yêu cầu phân phối");
                errorResponse.put("message", e.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            if (e.getMessage().contains("Can only set delivery date") ||
                e.getMessage().contains("must be in the future")) {
                errorResponse.put("error", "Không thể set ngày giao xe cho yêu cầu này");
                errorResponse.put("message", e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            errorResponse.put("error", "Có lỗi xảy ra khi set ngày giao xe");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/{requestId}/confirm-delivery")
    @Operation(summary = "Xác nhận đã nhận xe",
               description = "Dealer xác nhận đã nhận xe và cập nhật số lượng tồn kho")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Xác nhận nhận xe thành công"),
        @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
        @ApiResponse(responseCode = "403", description = "Không có quyền truy cập"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy yêu cầu"),
        @ApiResponse(responseCode = "400", description = "Yêu cầu không thể xác nhận")
    })
    public ResponseEntity<?> confirmDelivery(@PathVariable Integer requestId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Authorization header is required. Please login first to get JWT token.");
        }

        String email = authentication.getName();

        if (email == null || email.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Invalid authentication. Please login again.");
        }

        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        String roleName = user.getRoleId().getRoleName();

        if (!"DealerManager".equals(roleName) && !"DealerStaff".equals(roleName)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Access denied. Only Dealer Manager and Dealer Staff can confirm delivery.");
        }

        try {
            DistributionRequestResponseDto response = distributionRequestService.confirmDelivery(requestId, email);

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("message", "Đã xác nhận nhận xe thành công và cập nhật số lượng tồn kho");
            responseMap.put("data", response);

            return ResponseEntity.ok(responseMap);

        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();

            if (e.getMessage().contains("not found")) {
                errorResponse.put("error", "Không tìm thấy yêu cầu phân phối");
                errorResponse.put("message", e.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            if (e.getMessage().contains("Can only confirm delivery") ||
                e.getMessage().contains("does not belong to your dealer")) {
                errorResponse.put("error", "Không thể xác nhận nhận xe cho yêu cầu này");
                errorResponse.put("message", e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            errorResponse.put("error", "Có lỗi xảy ra khi xác nhận nhận xe");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

}
