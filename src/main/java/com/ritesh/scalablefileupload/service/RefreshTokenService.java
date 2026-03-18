package com.ritesh.scalablefileupload.service;

import com.ritesh.scalablefileupload.model.RefreshToken;
import com.ritesh.scalablefileupload.model.User;
import com.ritesh.scalablefileupload.repo.RefreshTokenRepo;
import com.ritesh.scalablefileupload.repo.UserRepo;
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

    public RefreshToken generateRefreshToken(User user){
        refreshTokenRepo.findByUser(user).ifPresent(refreshTokenRepo::delete);
        RefreshToken refreshToken = new RefreshToken();
        User verifiedUser = userRepo.findByUserEmail(user.getUserEmail());
       
        refreshToken.setToken(jwtService.generateRefreshToken(verifiedUser));

        refreshToken.setUser(verifiedUser);

        refreshToken.setExpiryDate(Instant.now().plusMillis(1000L * 60 * 60 * 24 * 7));

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
