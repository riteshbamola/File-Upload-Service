package com.ritesh.scalablefileupload.service;

import com.ritesh.scalablefileupload.model.RefreshToken;
import com.ritesh.scalablefileupload.model.User;
import com.ritesh.scalablefileupload.repo.RefreshTokenRepo;
import com.ritesh.scalablefileupload.repo.UserRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepo refreshTokenRepo;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private UserRepo userRepo;

    @Transactional
    public RefreshToken generateRefreshToken(String email){

        User user = userRepo.findByUserEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        refreshTokenRepo.findByUser(user).ifPresent(refreshTokenRepo::delete);
        refreshTokenRepo.flush();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(jwtService.generateRefreshToken(user));
        refreshToken.setUser(user);

        refreshToken.setExpiryDate(Instant.now().plusMillis(1000L * 60 * 60 * 24 * 7));
        System.out.println(refreshToken.getToken());
        System.out.println(refreshToken.getUser());
        return refreshTokenRepo.save(refreshToken);
    }

    public RefreshToken verifyRefreshToken(String token) {
        String tokenType = jwtService.extractTokenType(token);
        if (!"refresh".equals(tokenType)) {
            throw new RuntimeException("Invalid token type. Refresh token required.");
        }

        RefreshToken refreshToken = refreshTokenRepo.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepo.delete(refreshToken);
            throw new RuntimeException("Refresh token expired. Please login again.");
        }
        refreshTokenRepo.delete(refreshToken);
        return refreshToken;
    }

    public String generateAccessToken(RefreshToken verified){
        String accessToken = jwtService.generateToken(verified.getUser());
        return accessToken;
    }
}
