package org.example.service;

import org.example.dto.CreateUserAccountRequest;
import org.example.dto.DealerStaffResponse;
import org.example.dto.RoleResponse;
import org.example.dto.UpdateUserAccountRequest;
import org.example.dto.UserAccountResponse;

import java.util.List;

public interface UserAccountService {

    /**
     * Lấy tất cả user accounts với thông tin role và dealer
     */
    List<UserAccountResponse> getAllUsers();

    /**
     * Lấy thông tin user account theo ID
     */
    UserAccountResponse getUserById(Integer userId);

    /**
     * Tìm kiếm user accounts theo keyword
     */
    List<UserAccountResponse> searchUsers(String keyword);

    /**
     * Tìm kiếm user accounts theo role name
     */
    List<UserAccountResponse> searchUsersByRole(String roleName);

    /**
     * Tìm kiếm user accounts theo dealer name
     */
    List<UserAccountResponse> searchUsersByDealer(String dealerName);

    /**
     * Tìm kiếm user accounts theo cả role name và dealer name
     */
    List<UserAccountResponse> searchUsersByRoleAndDealer(String roleName, String dealerName);

    /**
     * Tạo user account mới với validation đầy đủ
     */
    UserAccountResponse createUserAccount(CreateUserAccountRequest request);

    /**
     * Cập nhật thông tin user account
     */
    UserAccountResponse updateUserAccount(Integer userId, UpdateUserAccountRequest request);

    /**
     * Xóa user account theo ID
     */
    void deleteUserAccount(Integer userId);

    /**
     * Lấy tất cả roles trong hệ thống
     */
    List<RoleResponse> getAllRoles();

    /**
     * Cập nhật thông tin cá nhân của user hiện tại đang đăng nhập
     * Dealer Manager và Dealer Staff chỉ có thể cập nhật: tên, số điện thoại, mật khẩu
     * Không được phép cập nhật: email, role, status, dealer
     */
    UserAccountResponse updateCurrentUserProfile(String currentUserEmail, UpdateUserAccountRequest request);

    /**
     * Lấy tất cả dealer staff của một dealer cụ thể
     */
    List<DealerStaffResponse> getDealerStaffByDealerName(String dealerName);

    /**
     * Lấy danh sách tên của tất cả dealer staff thuộc một dealer cụ thể
     */
    List<String> getDealerStaffNamesByDealerName(String dealerName);
}
