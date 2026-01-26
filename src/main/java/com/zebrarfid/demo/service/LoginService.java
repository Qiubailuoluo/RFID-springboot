package com.zebrarfid.demo.service;

import com.zebrarfid.demo.dto.login.LoginRequest;
import com.zebrarfid.demo.dto.login.LoginResponse;
import com.zebrarfid.demo.result.Result;

/**
 * 登录注册服务
 */
public interface LoginService {
    // 登录
    Result<LoginResponse> login(LoginRequest loginRequest);

}
