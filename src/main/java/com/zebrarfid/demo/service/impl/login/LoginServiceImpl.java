// src/main/java/com/zebrarfid/demo/service/impl/LoginServiceImpl.java
package com.zebrarfid.demo.service.impl.login;

import com.zebrarfid.demo.dto.login.LoginRequest;
import com.zebrarfid.demo.dto.login.LoginResponse;
import com.zebrarfid.demo.mapper.UserMapper;
import com.zebrarfid.demo.service.login.LoginService;
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
    private final UserMapper userMapper;

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
     * 根据用户名获取用户ID
     * 公有方法，用于非登入时获取用户ID
     */
    @Override
    public Long getUserIdByUsername(String username) {
        com.zebrarfid.demo.entity.User user = userMapper.selectByUsername(username);
        if (user != null) {
            return user.getId();
        }
        throw new RuntimeException("用户不存在: " + username);
    }

    /**
     * 从UserDetails获取用户ID
     * 注意：这里假设UserDetails实现类中有获取用户ID的方法
     */
    private Long getUserIdFromUserDetails(UserDetails userDetails) {
        try {
            com.zebrarfid.demo.entity.User user = userMapper.selectByUsername(userDetails.getUsername());
            if (user != null) {
                return user.getId();
            }
            throw new RuntimeException("用户不存在: " + userDetails.getUsername());
        } catch (Exception e) {
            // 记录异常信息以便调试
            System.out.println("获取用户ID时发生异常: " + e.getMessage());
            throw e;
        }
        //通过用户名查询用户ID
        //return 1L;

    }
}
