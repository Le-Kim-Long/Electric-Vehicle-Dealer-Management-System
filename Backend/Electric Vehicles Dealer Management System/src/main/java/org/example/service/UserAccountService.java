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
     * Tạo user account mới với validation đầy đủ
     */
    public UserAccountResponse createUserAccount(CreateUserAccountRequest request) {
        // Validate input
        validateCreateUserRequest(request);

        // Kiểm tra email đã tồn tại chưa
        Optional<UserAccount> existingUserByEmail = userAccountRepository.findByEmail(request.getEmail());
        if (existingUserByEmail.isPresent()) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }

        // Kiểm tra số điện thoại đã tồn tại chưa
        Optional<UserAccount> existingUserByPhone = userAccountRepository.findByPhoneNumber(request.getPhoneNumber());
        if (existingUserByPhone.isPresent()) {
            throw new RuntimeException("Phone number already exists: " + request.getPhoneNumber());
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
        newUser.setPhoneNumber(request.getPhoneNumber());
        newUser.setRoleId(role);
        newUser.setDealer(dealer);
        // Status sẽ được set default value "Active" bởi database
        newUser.setStatus("Active");
        newUser.setCreatedDate(LocalDateTime.now());

        // Lưu vào database
        UserAccount savedUser = userAccountRepository.save(newUser);

        // Refresh entity để lấy giá trị default từ database (như status)
        userAccountRepository.flush();
        UserAccount refreshedUser = userAccountRepository.findById(savedUser.getUserId())
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
        if ("Admin".equals(existingUser.getRoleId().getRoleName())) {
            throw new RuntimeException("Cannot update Admin account. Admin accounts are protected from modifications.");
        }

        // Validate input
        validateUpdateUserRequest(request);

        // Cập nhật các field nếu có giá trị mới
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            // Kiểm tra username đã tồn tại chưa (trừ user hiện tại)
            existingUser.setUsername(request.getUsername());
        }

        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            existingUser.setPassword(request.getPassword());
        }

        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            // Kiểm tra email đã tồn tại chưa (trừ user hiện tại)
            Optional<UserAccount> existingUserByEmail = userAccountRepository.findByEmail(request.getEmail());
            if (existingUserByEmail.isPresent() && !existingUserByEmail.get().getUserId().equals(userId)) {
                throw new RuntimeException("Email already exists: " + request.getEmail());
            }
            existingUser.setEmail(request.getEmail());
        }

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            // Kiểm tra số điện thoại đã tồn tại chưa (trừ user hiện tại)
            Optional<UserAccount> existingUserByPhone = userAccountRepository.findByPhoneNumber(request.getPhoneNumber());
            if (existingUserByPhone.isPresent() && !existingUserByPhone.get().getUserId().equals(userId)) {
                throw new RuntimeException("Phone number already exists: " + request.getPhoneNumber());
            }
            existingUser.setPhoneNumber(request.getPhoneNumber());
        }

        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            existingUser.setStatus(request.getStatus());
        }

        // Cập nhật role nếu có
        if (request.getRoleName() != null && !request.getRoleName().trim().isEmpty()) {
            Role newRole = roleRepository.findByRoleName(request.getRoleName())
                    .orElseThrow(() -> new RuntimeException("Role not found: " + request.getRoleName()));
            existingUser.setRoleId(newRole);

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
                String currentRole = existingUser.getRoleId().getRoleName();
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
     * Xóa user account theo ID
     */
    public void deleteUserAccount(Integer userId) {
        // Tìm user theo ID
        UserAccount existingUser = userAccountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Kiểm tra không cho phép xóa Admin account
        if ("Admin".equals(existingUser.getRoleId().getRoleName())) {
            throw new RuntimeException("Cannot delete Admin account. Admin accounts are protected from deletion.");
        }

        // Xóa user account
        userAccountRepository.deleteById(userId);
    }

    /**
     * Validate request tạo user account với các yêu cầu:
     * - Tên người dùng không có ký tự đặc biệt và số
     * - Mật khẩu ít nhất 6 ký tự
     * - Số điện thoại phải đúng 10 số
     * - Email và số điện thoại là duy nhất không trùng
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

        // Validate username: chỉ chứa chữ cái (không có số và ký tự đặc biệt)
        String username = request.getUsername().trim();
        if (!username.matches("^[a-zA-ZàáảãạâấầẩẫậăắằẳẵặèéẻẽẹêếềểễệìíỉĩịòóỏõọôốồổỗộơớờởỡợùúủũụưứừửữựỳýỷỹỵđĐ\\s]+$")) {
            throw new RuntimeException("Username must contain only letters (no numbers or special characters)");
        }

        // Validate password: ít nhất 6 ký tự
        if (request.getPassword().length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters long");
        }

        // Validate phone number: đúng 10 số
        String phoneNumber = request.getPhoneNumber().trim();
        if (!phoneNumber.matches("^\\d{10}$")) {
            throw new RuntimeException("Phone number must be exactly 10 digits");
        }

        // Validate email format
        String email = request.getEmail().trim();
        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            throw new RuntimeException("Invalid email format");
        }

        // Validate role
        if (!"DealerStaff".equals(request.getRoleName()) &&
            !"EVMStaff".equals(request.getRoleName()) &&
            !"DealerManager".equals(request.getRoleName())) {
            throw new RuntimeException("Invalid role. Valid roles are: DealerStaff, EVMStaff, DealerManager");
        }
    }

    /**
     * Validate request cập nhật user account
     */
    private void validateUpdateUserRequest(UpdateUserAccountRequest request) {
        if (request == null) {
            throw new RuntimeException("Update user request cannot be null");
        }

        // Validate username nếu có: chỉ chứa chữ cái (không có số và ký tự đặc biệt)
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            String username = request.getUsername().trim();
            if (!username.matches("^[a-zA-ZàáảãạâấầẩẫậăắằẳẵặèéẻẽẹêếềểễệìíỉĩịòóỏõọôốồổỗộơớờởỡợùúủũụưứừửữựỳýỷỹỵđĐ\\s]+$")) {
                throw new RuntimeException("Username must contain only letters (no numbers or special characters)");
            }
        }

        // Validate password length nếu có: ít nhất 6 ký tự
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty() && request.getPassword().length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters long");
        }

        // Validate phone number nếu có: đúng 10 số
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            String phoneNumber = request.getPhoneNumber().trim();
            if (!phoneNumber.matches("^\\d{10}$")) {
                throw new RuntimeException("Phone number must be exactly 10 digits");
            }
        }

        // Validate email format nếu có
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            String email = request.getEmail().trim();
            if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
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
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .password(user.getPassword()) // Thêm password vào response
                .phoneNumber(user.getPhoneNumber())
                .status(user.getStatus())
                .createdDate(user.getCreatedDate())
                .roleName(user.getRoleId() != null ? user.getRoleId().getRoleName() : null)
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
                .roleId(role.getRoleId())
                .roleName(role.getRoleName())
                .build();
    }

    /**
     * Cập nhật thông tin cá nhân của user hiện tại đang đăng nhập
     * Dealer Manager và Dealer Staff chỉ có thể cập nhật: tên, số điện thoại, mật khẩu
     * Không được phép cập nhật: email, role, status, dealer
     */
    public UserAccountResponse updateCurrentUserProfile(String currentUserEmail, UpdateUserAccountRequest request) {
        // Tìm user hiện tại theo email
        UserAccount currentUser = userAccountRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("Current user not found with email: " + currentUserEmail));

        String currentUserRole = currentUser.getRoleId().getRoleName(); // Changed from getRole_id().getRole_name()

        // Kiểm tra role có được phép cập nhật profile không
        if (!"DealerManager".equals(currentUserRole) && !"DealerStaff".equals(currentUserRole)) {
            throw new RuntimeException("Access denied. Only DealerManager and DealerStaff can update their profile.");
        }

        // Validate input cho profile update (chỉ validate các field được phép)
        validateProfileUpdateRequest(request, currentUserRole);

        // Kiểm tra các field không được phép cập nhật
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Access denied. " + currentUserRole + " cannot update email.");
        }
        if (request.getRoleName() != null && !request.getRoleName().trim().isEmpty()) {
            throw new RuntimeException("Access denied. " + currentUserRole + " cannot update role.");
        }
        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            throw new RuntimeException("Access denied. " + currentUserRole + " cannot update status.");
        }
        if (request.getDealerName() != null && !request.getDealerName().trim().isEmpty()) {
            throw new RuntimeException("Access denied. " + currentUserRole + " cannot update dealer assignment.");
        }

        // Cập nhật các field được phép
        boolean hasChanges = false;

        // Cập nhật username nếu có
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            // Kiểm tra username đã tồn tại chưa (trừ user hiện tại)
            Optional<UserAccount> existingUserByUsername = userAccountRepository.findByUsername(request.getUsername());
            if (existingUserByUsername.isPresent() && !existingUserByUsername.get().getUserId().equals(currentUser.getUserId())) { // Changed from getUser_id
                throw new RuntimeException("Username already exists: " + request.getUsername());
            }
            currentUser.setUsername(request.getUsername().trim());
            hasChanges = true;
        }

        // Cập nhật password nếu có
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            currentUser.setPassword(request.getPassword());
            hasChanges = true;
        }

        // Cập nhật phone number nếu có
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            // Kiểm tra số điện thoại đã tồn tại chưa (trừ user hiện tại)
            Optional<UserAccount> existingUserByPhone = userAccountRepository.findByPhoneNumber(request.getPhoneNumber());
            if (existingUserByPhone.isPresent() && !existingUserByPhone.get().getUserId().equals(currentUser.getUserId())) { // Changed from getUser_id
                throw new RuntimeException("Phone number already exists: " + request.getPhoneNumber());
            }
            currentUser.setPhoneNumber(request.getPhoneNumber().trim()); // Changed from setPhone_number
            hasChanges = true;
        }

        if (!hasChanges) {
            throw new RuntimeException("No valid fields to update. You can only update: username, password, phoneNumber");
        }

        // Lưu thay đổi
        UserAccount updatedUser = userAccountRepository.save(currentUser);

        // Convert sang response DTO
        return convertToUserAccountResponse(updatedUser);
    }

    /**
     * Validate request cập nhật profile cho dealer manager và dealer staff
     */
    private void validateProfileUpdateRequest(UpdateUserAccountRequest request, String userRole) {
        if (request == null) {
            throw new RuntimeException("Update profile request cannot be null");
        }

        // Validate username nếu có: chỉ chứa chữ cái (không có số và ký tự đặc biệt)
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            String username = request.getUsername().trim();
            if (!username.matches("^[a-zA-ZàáảãạâấầẩẫậăắằẳẵặèéẻẽẹêếềểễệìíỉĩịòóỏõọôốồổỗộơớờởỡợùúủũụưứừửữựỳýỷỹỵđĐ\\s]+$")) {
                throw new RuntimeException("Username must contain only letters (no numbers or special characters)");
            }
        }

        // Validate password length nếu có: ít nhất 6 ký tự
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty() && request.getPassword().length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters long");
        }

        // Validate phone number nếu có: đúng 10 số
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            String phoneNumber = request.getPhoneNumber().trim();
            if (!phoneNumber.matches("^\\d{10}$")) {
                throw new RuntimeException("Phone number must be exactly 10 digits");
            }
        }
    }
}
