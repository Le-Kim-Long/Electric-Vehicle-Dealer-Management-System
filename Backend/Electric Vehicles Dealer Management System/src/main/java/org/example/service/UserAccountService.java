package org.example.service;

import org.example.dto.CreateUserAccountRequest;
import org.example.dto.RoleResponse;
import org.example.dto.UpdateUserAccountRequest;
import org.example.dto.UserAccountResponse;
import org.example.entity.Dealer;
import org.example.entity.Role;
import org.example.entity.UserAccount;
import org.example.repository.DealerRepository;
import org.example.repository.RoleRepository;
import org.example.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserAccountService {

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DealerRepository dealerRepository;

    /**
     * Lấy tất cả user accounts với thông tin role và dealer
     */
    public List<UserAccountResponse> getAllUsers() {
        List<UserAccount> users = userAccountRepository.findAllUsersWithRoleAndDealer();

        return users.stream()
                .map(this::convertToUserAccountResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy thông tin user account theo ID
     */
    public UserAccountResponse getUserById(Integer userId) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        return convertToUserAccountResponse(user);
    }

    /**
     * Tìm kiếm user accounts theo keyword
     */
    public List<UserAccountResponse> searchUsers(String keyword) {
        List<UserAccount> users = userAccountRepository.searchUsersWithRoleAndDealer(keyword);

        return users.stream()
                .map(this::convertToUserAccountResponse)
                .collect(Collectors.toList());
    }

    /**
     * Tìm kiếm user accounts theo role name
     */
    public List<UserAccountResponse> searchUsersByRole(String roleName) {
        List<UserAccount> users = userAccountRepository.findUsersByRoleName(roleName);

        return users.stream()
                .map(this::convertToUserAccountResponse)
                .collect(Collectors.toList());
    }

    /**
     * Tìm kiếm user accounts theo dealer name
     */
    public List<UserAccountResponse> searchUsersByDealer(String dealerName) {
        List<UserAccount> users = userAccountRepository.findUsersByDealerName(dealerName);

        return users.stream()
                .map(this::convertToUserAccountResponse)
                .collect(Collectors.toList());
    }

    /**
     * Tìm kiếm user accounts theo cả role name và dealer name
     */
    public List<UserAccountResponse> searchUsersByRoleAndDealer(String roleName, String dealerName) {
        List<UserAccount> users = userAccountRepository.findUsersByRoleAndDealer(roleName, dealerName);

        return users.stream()
                .map(this::convertToUserAccountResponse)
                .collect(Collectors.toList());
    }

    /**
     * Tạo user account mới
     */
    public UserAccountResponse createUserAccount(CreateUserAccountRequest request) {
        // Validate input
        validateCreateUserRequest(request);

        // Kiểm tra username đã tồn tại chưa
        Optional<UserAccount> existingUserByUsername = userAccountRepository.findByUsername(request.getUsername());
        if (existingUserByUsername.isPresent()) {
            throw new RuntimeException("Username already exists: " + request.getUsername());
        }

        // Kiểm tra email đã tồn tại chưa
        Optional<UserAccount> existingUserByEmail = userAccountRepository.findByEmail(request.getEmail());
        if (existingUserByEmail.isPresent()) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }

        // Lấy role theo tên
        Role role = roleRepository.findByRoleName(request.getRoleName())
                .orElseThrow(() -> new RuntimeException("Role not found: " + request.getRoleName()));

        // Lấy dealer nếu cần thiết (theo tên thay vì ID)
        Dealer dealer = null;
        if ("DealerStaff".equals(request.getRoleName()) || "DealerManager".equals(request.getRoleName())) {
            if (request.getDealerName() == null || request.getDealerName().trim().isEmpty()) {
                throw new RuntimeException("Dealer name is required for role: " + request.getRoleName());
            }
            dealer = dealerRepository.findByDealerName(request.getDealerName().trim())
                    .orElseThrow(() -> new RuntimeException("Dealer not found with name: " + request.getDealerName()));
        } else if ("EVMStaff".equals(request.getRoleName()) && request.getDealerName() != null && !request.getDealerName().trim().isEmpty()) {
            throw new RuntimeException("EVMStaff role should not have a dealer assigned");
        }

        // Tạo UserAccount entity
        UserAccount newUser = new UserAccount();
        newUser.setUsername(request.getUsername());
        newUser.setPassword(request.getPassword()); // Lưu password thông thường, không mã hóa
        newUser.setEmail(request.getEmail());
        newUser.setPhone_number(request.getPhoneNumber());
        newUser.setRole_id(role);
        newUser.setDealer(dealer);
        // Status sẽ được set default value "Active" bởi database
        newUser.setStatus("Active");
        newUser.setCreated_date(LocalDateTime.now());

        // Lưu vào database
        UserAccount savedUser = userAccountRepository.save(newUser);

        // Refresh entity để lấy giá trị default từ database (như status)
        userAccountRepository.flush();
        UserAccount refreshedUser = userAccountRepository.findById(savedUser.getUser_id())
                .orElse(savedUser);

        // Convert sang response DTO
        return convertToUserAccountResponse(refreshedUser);
    }

    /**
     * Cập nhật thông tin user account
     */
    public UserAccountResponse updateUserAccount(Integer userId, UpdateUserAccountRequest request) {
        // Tìm user theo ID
        UserAccount existingUser = userAccountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Kiểm tra không cho phép update Admin account
        if ("Admin".equals(existingUser.getRole_id().getRole_name())) {
            throw new RuntimeException("Cannot update Admin account. Admin accounts are protected from modifications.");
        }

        // Validate input
        validateUpdateUserRequest(request);

        // Cập nhật các field nếu có giá trị mới
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            // Kiểm tra username đã tồn tại chưa (trừ user hiện tại)
            Optional<UserAccount> existingUserByUsername = userAccountRepository.findByUsername(request.getUsername());
            if (existingUserByUsername.isPresent() && !existingUserByUsername.get().getUser_id().equals(userId)) {
                throw new RuntimeException("Username already exists: " + request.getUsername());
            }
            existingUser.setUsername(request.getUsername());
        }

        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            existingUser.setPassword(request.getPassword());
        }

        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            // Kiểm tra email đã tồn tại chưa (trừ user hiện tại)
            Optional<UserAccount> existingUserByEmail = userAccountRepository.findByEmail(request.getEmail());
            if (existingUserByEmail.isPresent() && !existingUserByEmail.get().getUser_id().equals(userId)) {
                throw new RuntimeException("Email already exists: " + request.getEmail());
            }
            existingUser.setEmail(request.getEmail());
        }

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            existingUser.setPhone_number(request.getPhoneNumber());
        }

        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            existingUser.setStatus(request.getStatus());
        }

        // Cập nhật role nếu có
        if (request.getRoleName() != null && !request.getRoleName().trim().isEmpty()) {
            Role newRole = roleRepository.findByRoleName(request.getRoleName())
                    .orElseThrow(() -> new RuntimeException("Role not found: " + request.getRoleName()));
            existingUser.setRole_id(newRole);

            // Xử lý dealer khi role thay đổi
            if ("DealerStaff".equals(request.getRoleName()) || "DealerManager".equals(request.getRoleName())) {
                if (request.getDealerName() != null && !request.getDealerName().trim().isEmpty()) {
                    Dealer dealer = dealerRepository.findByDealerName(request.getDealerName().trim())
                            .orElseThrow(() -> new RuntimeException("Dealer not found with name: " + request.getDealerName()));
                    existingUser.setDealer(dealer);
                } else if (existingUser.getDealer() == null) {
                    // Nếu đổi sang role cần dealer nhưng chưa có dealer thì báo lỗi
                    throw new RuntimeException("Dealer name is required for role: " + request.getRoleName());
                }
                // Nếu đã có dealer từ trước và không cung cấp dealerName mới thì giữ nguyên dealer cũ
            } else if ("EVMStaff".equals(request.getRoleName())) {
                // EVMStaff không cần dealer
                existingUser.setDealer(null);
            }
        } else {
            // Nếu không update role nhưng có dealerName thì chỉ update dealer
            if (request.getDealerName() != null && !request.getDealerName().trim().isEmpty()) {
                String currentRole = existingUser.getRole_id().getRole_name();
                if ("DealerStaff".equals(currentRole) || "DealerManager".equals(currentRole)) {
                    Dealer dealer = dealerRepository.findByDealerName(request.getDealerName().trim())
                            .orElseThrow(() -> new RuntimeException("Dealer not found with name: " + request.getDealerName()));
                    existingUser.setDealer(dealer);
                } else {
                    throw new RuntimeException("Cannot assign dealer to role: " + currentRole);
                }
            }
        }

        // Lưu thay đổi
        UserAccount updatedUser = userAccountRepository.save(existingUser);

        // Convert sang response DTO
        return convertToUserAccountResponse(updatedUser);
    }

    /**
     * Validate request tạo user account
     */
    private void validateCreateUserRequest(CreateUserAccountRequest request) {
        if (request == null) {
            throw new RuntimeException("Create user request cannot be null");
        }

        // Kiểm tra các trường bắt buộc
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new RuntimeException("Username is required");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new RuntimeException("Password is required");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }
        if (request.getRoleName() == null || request.getRoleName().trim().isEmpty()) {
            throw new RuntimeException("Role name is required");
        }
        if (request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty()) {
            throw new RuntimeException("Phone number is required");
        }

        // Validate role
        if (!"DealerStaff".equals(request.getRoleName()) &&
            !"EVMStaff".equals(request.getRoleName()) &&
            !"DealerManager".equals(request.getRoleName())) {
            throw new RuntimeException("Invalid role. Valid roles are: DealerStaff, EVMStaff, DealerManager");
        }

        // Validate password length
        if (request.getPassword().length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters long");
        }

        // Validate email format (basic)
        if (!request.getEmail().contains("@") || !request.getEmail().contains(".")) {
            throw new RuntimeException("Invalid email format");
        }
    }

    /**
     * Validate request cập nhật user account
     */
    private void validateUpdateUserRequest(UpdateUserAccountRequest request) {
        if (request == null) {
            throw new RuntimeException("Update user request cannot be null");
        }

        // Validate password length nếu có
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty() && request.getPassword().length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters long");
        }

        // Validate email format nếu có
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (!request.getEmail().contains("@") || !request.getEmail().contains(".")) {
                throw new RuntimeException("Invalid email format");
            }
        }

        // Validate role nếu có
        if (request.getRoleName() != null && !request.getRoleName().trim().isEmpty()) {
            if (!"DealerStaff".equals(request.getRoleName()) &&
                !"EVMStaff".equals(request.getRoleName()) &&
                !"DealerManager".equals(request.getRoleName())) {
                throw new RuntimeException("Invalid role. Valid roles are: DealerStaff, EVMStaff, DealerManager");
            }
        }

        // Validate status nếu có
        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            if (!"Active".equals(request.getStatus()) && !"Inactive".equals(request.getStatus())) {
                throw new RuntimeException("Invalid status. Valid statuses are: Active, Inactive");
            }
        }
    }

    /**
     * Convert UserAccount entity sang UserAccountResponse DTO
     */
    private UserAccountResponse convertToUserAccountResponse(UserAccount user) {
        return UserAccountResponse.builder()
                .userId(user.getUser_id())
                .username(user.getUsername())
                .email(user.getEmail())
                .password(user.getPassword()) // Thêm password vào response
                .phoneNumber(user.getPhone_number())
                .status(user.getStatus())
                .createdDate(user.getCreated_date())
                .roleName(user.getRole_id() != null ? user.getRole_id().getRole_name() : null)
                .dealerName(user.getDealer() != null ? user.getDealer().getDealerName() : null)
                .dealerId(user.getDealer() != null ? user.getDealer().getDealerId() : null)
                .build();
    }

    /**
     * Lấy tất cả roles trong hệ thống
     */
    public List<RoleResponse> getAllRoles() {
        List<Role> roles = roleRepository.findAll();

        return roles.stream()
                .map(this::convertToRoleResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert Role entity sang RoleResponse DTO
     */
    private RoleResponse convertToRoleResponse(Role role) {
        return RoleResponse.builder()
                .roleId(role.getRole_id())
                .roleName(role.getRole_name())
                .build();
    }
}
