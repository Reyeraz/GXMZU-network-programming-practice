package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.dto.UserLoginRequest;
import com.example.demo.dto.UserRegisterRequest;
import com.example.demo.exception.BadRequestException;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.User;
import com.example.demo.service.UserService;
import com.example.demo.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void register(UserRegisterRequest request) {
        LambdaQueryWrapper<User> usernameWrapper = new LambdaQueryWrapper<>();
        usernameWrapper.eq(User::getUsername, request.getUsername());
        if (userMapper.selectCount(usernameWrapper) > 0) {
            throw new BadRequestException("用户名已存在");
        }

        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            LambdaQueryWrapper<User> phoneWrapper = new LambdaQueryWrapper<>();
            phoneWrapper.eq(User::getPhone, request.getPhone());
            if (userMapper.selectCount(phoneWrapper) > 0) {
                throw new BadRequestException("手机号已被注册");
            }
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setAddress(request.getAddress());
        user.setPrivilege(0);
        user.setStatus(1);

        userMapper.insert(user);
    }

    @Override
    public String login(UserLoginRequest request) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new BadRequestException("用户名或密码错误");
        }

        if (user.getStatus() == 0) {
            throw new BadRequestException("用户已被禁用");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("用户名或密码错误");
        }

        return JwtUtil.generateToken(user.getUserId(), user.getUsername());
    }
}
