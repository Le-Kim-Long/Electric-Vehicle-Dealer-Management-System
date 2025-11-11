package org.example.service.implementation;

import lombok.RequiredArgsConstructor;
import org.example.dto.LoginRequest;
import org.example.dto.LoginResponse;
import org.example.entity.UserAccount;
import org.example.repository.UserAccountRepository;
import org.example.security.JwtUtil;
import org.example.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserAccountRepository userRepo;
    private final JwtUtil jwtUtil;

    @Override
    public LoginResponse login(LoginRequest request) {
        // Validate input
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        }

        // Kiểm tra xem email có tồn tại không
        Optional<UserAccount> userOptional = userRepo.findByEmail(request.getEmail().trim());
        if (userOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account does not exist");
        }

        UserAccount user = userOptional.get();

        // Kiểm tra password
        if (!Objects.equals(user.getPassword().trim(), request.getPassword().trim())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect password");
        }

        // Kiểm tra status của account - chỉ cho phép login khi status là "Active"
        if (!"Active".equals(user.getStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is not active or has been suspended");
        }

        // Kiểm tra role và xử lý dealer name phù hợp
        String roleName = user.getRoleId().getRoleName();
        String dealerName = null;

        // Admin và EVMStaff không thuộc dealer nào (dealerId = null)
        if ("Admin".equals(roleName) || "EVMStaff".equals(roleName)) {
            dealerName = null; // Hoặc có thể set thành "System" tùy theo business logic
        } else if ("DealerStaff".equals(roleName) || "DealerManager".equals(roleName)) {
            // DealerStaff và DealerManager phải thuộc một dealer
            if (user.getDealer() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Dealer account is not associated with any dealer");
            }
            dealerName = user.getDealer().getDealerName();
        } else {
            // Các role khác (nếu có)
            dealerName = user.getDealer() != null ? user.getDealer().getDealerName() : null;
        }

        String token = jwtUtil.generateToken(user.getEmail(), roleName, dealerName);

        return new LoginResponse(
                token,
                roleName,
                user.getUsername(),
                user.getStatus(),
                dealerName
        );
    }
}