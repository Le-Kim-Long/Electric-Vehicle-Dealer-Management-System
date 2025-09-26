package org.example.service.implementation;

import lombok.RequiredArgsConstructor;
import org.example.dto.LoginRequest;
import org.example.dto.LoginResponse;
import org.example.entity.NguoiDung;
import org.example.repository.NguoiDungRepository;
import org.example.security.JwtUtil;
import org.example.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final NguoiDungRepository userRepo;
    private final JwtUtil jwtUtil;

    @Override
    public LoginResponse login(LoginRequest request) {
        NguoiDung user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        // Plain text check (since DB has "123456" in sample data)
        if (!Objects.equals(user.getMatKhau().trim(), request.getPassword().trim())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getVaiTro().getTenVaiTro());

        return new LoginResponse(
                token,
                user.getVaiTro().getTenVaiTro(),
                user.getHoTen());
    }
}