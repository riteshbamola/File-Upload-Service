package com.ritesh.scalablefileupload.service;

import com.ritesh.scalablefileupload.model.User;
import com.ritesh.scalablefileupload.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private AuthenticationManager authManager;
    @Autowired
    private JWTService jWTService;

    public User register(User user){
        return userRepo.save(user);
    }


    public String verify(User user) {
        Authentication authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(),user.getUserpaswword()));

        if(authentication.isAuthenticated()){
            return jWTService.generateToken(user);
        }
        return "failed";
    }
}
