// src/main/java/com/zebrarfid/demo/service/impl/LoginServiceImpl.java
package com.zebrarfid.demo.service.impl;

import com.zebrarfid.demo.dto.login.LoginRequest;
import com.zebrarfid.demo.dto.login.LoginResponse;
import com.zebrarfid.demo.service.LoginService;
import com.zebrarfid.demo.result.Result;
import com.zebrarfid.demo.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Override
    public Result<LoginResponse> login(LoginRequest loginRequest) {
        try {
            // 1. 用户名密码认证
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );

            // 2. 获取用户信息，生成JWT
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);

            // 3. 构建返回结果
            LoginResponse response = new LoginResponse(
                token,
                getUserIdFromUserDetails(userDetails), // 假设有获取用户ID的方法
                userDetails.getUsername()
            );

            return Result.success("登录成功", response);
        } catch (Exception e) {
            return Result.error(401, "账号或密码错误");
        }
    }

    /**
     * 从UserDetails获取用户ID
     * 注意：这里假设UserDetails实现类中有获取用户ID的方法
     */
    private Long getUserIdFromUserDetails(UserDetails userDetails) {
        // 根据实际的UserDetails实现来获取用户ID
        // 这里只是一个示例，需要根据实际情况调整
        return 1L; // 临时返回值，需要根据实际业务逻辑修改
    }
}
