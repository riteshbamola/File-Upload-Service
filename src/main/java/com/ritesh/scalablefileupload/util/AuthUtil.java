package com.ritesh.scalablefileupload.util;

import com.ritesh.scalablefileupload.model.User;
import com.ritesh.scalablefileupload.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthUtil {

    @Autowired
    private UserRepo userRepo;


    public String getCurrentUserEmail() {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
    }


    public User getCurrentUser() {
        String email = getCurrentUserEmail();
        return userRepo.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}