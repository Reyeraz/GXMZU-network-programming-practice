package com.example.demo.service;

import com.example.demo.dto.UserLoginRequest;
import com.example.demo.dto.UserRegisterRequest;

public interface UserService {
    void register(UserRegisterRequest request);
    String login(UserLoginRequest request);
}
