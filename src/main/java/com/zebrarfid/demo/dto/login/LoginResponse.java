package com.zebrarfid.demo.dto.login;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登入响应数据
 * 携带token 和 用户信息
 */
@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private Long userId;
    private String username;
}
