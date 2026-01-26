package com.zebrarfid.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zebrarfid.demo.dto.login.RegisterRequest;
import com.zebrarfid.demo.entity.User;
import com.zebrarfid.demo.mapper.UserMapper;
import com.zebrarfid.demo.result.Result;
import com.zebrarfid.demo.service.RegisterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 注册服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterServiceImpl implements RegisterService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Result<?> register(RegisterRequest registerRequest) {
        // 1. 校验参数（非空）
        if (registerRequest.getUsername() == null || registerRequest.getUsername().trim().isEmpty()) {
            return Result.error("用户名不能为空");
        }
        if (registerRequest.getPassword() == null || registerRequest.getPassword().trim().isEmpty()) {
            return Result.error("密码不能为空");
        }
        if (registerRequest.getNickname() == null || registerRequest.getNickname().trim().isEmpty()) {
            return Result.error("昵称不能为空");
        }

        // 2. 校验用户名是否已存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>()
                .eq(User::getUsername, registerRequest.getUsername().trim());
        User existUser = userMapper.selectOne(queryWrapper);
        if (existUser != null) {
            return Result.error("用户名已存在，请更换");
        }

        // 3. 密码加密（BCrypt）
        String encryptPassword = passwordEncoder.encode(registerRequest.getPassword().trim());

        // 4. 构建User对象并保存
        User user = new User();
        user.setUsername(registerRequest.getUsername().trim());
        user.setPassword(encryptPassword);
        user.setNickname(registerRequest.getNickname().trim());
        user.setStatus(1); // 1-正常状态

        try {
            userMapper.insert(user);
            log.info("用户注册成功，用户名：{}", registerRequest.getUsername());
            return Result.success("注册成功");
        } catch (Exception e) {
            log.error("用户注册失败", e);
            return Result.error("注册失败，请重试");
        }
    }
}
