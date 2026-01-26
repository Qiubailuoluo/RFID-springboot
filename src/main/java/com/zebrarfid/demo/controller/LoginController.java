package com.zebrarfid.demo.controller;

import com.zebrarfid.demo.dto.login.LoginRequest;
import com.zebrarfid.demo.dto.login.LoginResponse;
import com.zebrarfid.demo.dto.login.RegisterRequest;
import com.zebrarfid.demo.result.Result;
import com.zebrarfid.demo.service.LoginService;
import com.zebrarfid.demo.service.RegisterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LoginController {

    // 注入service层接口
    private final LoginService loginService;
    private final RegisterService registerService; // 添加RegisterService注入

    /**
     * 登录接口
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        return loginService.login(loginRequest);
    }

    /**
     * 注册接口
     */
    @PostMapping("/register")
    public Result<?> register(@RequestBody RegisterRequest registerRequest) {
        return registerService.register(registerRequest);
    }

}
