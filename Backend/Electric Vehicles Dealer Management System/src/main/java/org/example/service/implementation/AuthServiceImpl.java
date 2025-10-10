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

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserAccountRepository userRepo;
    private final JwtUtil jwtUtil;

    @Override
    public LoginResponse login(LoginRequest request) {
        UserAccount user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        // Plain text check (since DB has "123456" in sample data)
        if (!Objects.equals(user.getPassword().trim(), request.getPassword().trim())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        // Kiểm tra role và xử lý dealer name phù hợp
        String roleName = user.getRole_id().getRole_name();
        String dealerName = null;

        // Admin và EVMStaff không thuộc dealer nào (dealerId = null)
        if ("Admin".equals(roleName) || "EVMStaff".equals(roleName)) {
            dealerName = null; // Hoặc có thể set thành "System" tùy theo business logic
        } else if ("DealerStaff".equals(roleName)) {
            // DealerStaff phải thuộc một dealer
            if (user.getDealer() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Dealer staff account is not associated with any dealer");
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