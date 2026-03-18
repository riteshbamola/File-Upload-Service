package com.ritesh.scalablefileupload.service;

import com.ritesh.scalablefileupload.model.User;
import com.ritesh.scalablefileupload.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private JWTService jWTService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public User register(User user) {
        if(userRepo.existsUserByUserEmail(user.getUserEmail())){
            throw new RuntimeException("Email already exists: " + user.getUserEmail());
        }
        user.setUserPassword(passwordEncoder.encode(user.getUserPassword()));
        return userRepo.save(user);
    }

    public String verify(String userEmail, String userPassword) {

        if(!userRepo.existsUserByUserEmail(userEmail)){
            throw new RuntimeException("Email Not Found" );
        }
        User dbUser = userRepo.findByUserEmail(userEmail).orElseThrow(()->new RuntimeException("User Not Found"));

        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(userEmail,userPassword)
        );
        if (authentication.isAuthenticated()) {
            return jWTService.generateToken(dbUser);
        }
        return "failed";
    }

    public List<User> getUsers() {
        List<User> users = userRepo.findAll();
        return users;
    }
}