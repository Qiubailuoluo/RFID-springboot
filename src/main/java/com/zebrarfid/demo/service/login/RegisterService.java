package com.zebrarfid.demo.service.login;

import com.zebrarfid.demo.dto.login.RegisterRequest;
import com.zebrarfid.demo.result.Result;

/**
 * 注册服务接口
 */
public interface RegisterService {
    Result<?> register(RegisterRequest registerRequest);
}
