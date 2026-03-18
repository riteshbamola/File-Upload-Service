package com.ritesh.scalablefileupload.controller;

import com.ritesh.scalablefileupload.model.User;
import com.ritesh.scalablefileupload.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        User savedUser = userService.register(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        System.out.println(user);
        String token = userService.verify(user);
        return ResponseEntity.status(HttpStatus.OK).body(token);
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsers(){
        List<User> users = userService.getUsers();
        return ResponseEntity.status(HttpStatus.OK).body(users);
    }



}
