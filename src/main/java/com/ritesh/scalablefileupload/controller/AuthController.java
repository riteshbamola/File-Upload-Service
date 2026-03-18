package com.ritesh.scalablefileupload.controller;

import com.ritesh.scalablefileupload.model.RefreshToken;
import com.ritesh.scalablefileupload.model.User;
import com.ritesh.scalablefileupload.service.RefreshTokenService;
import com.ritesh.scalablefileupload.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class AuthController {

    @Autowired
    private UserService userService;
    @Autowired
    private RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        User savedUser = userService.register(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String,String> body) {

        String userEmail = body.get("email");
        String userPassword = body.get("password");

        String accessToken = userService.verify(userEmail,userPassword);
        String refreshToken = refreshTokenService.generateRefreshToken(userEmail).getToken();

        return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken
        ));
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsers(){
        List<User> users = userService.getUsers();
        return ResponseEntity.status(HttpStatus.OK).body(users);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request){
        String refreshToken = request.get("refreshToken");

        RefreshToken verified = refreshTokenService.verifyRefreshToken(refreshToken);
        String userEmail = verified.getUser().getUserEmail();

        String newAccessToken = refreshTokenService.generateAccessToken(verified);
        String newRefreshToken = refreshTokenService.generateRefreshToken(userEmail).getToken();
        return ResponseEntity.ok(Map.of(
                "accessToken", newAccessToken,
                "refreshToken", newRefreshToken
        ));

    }



}
