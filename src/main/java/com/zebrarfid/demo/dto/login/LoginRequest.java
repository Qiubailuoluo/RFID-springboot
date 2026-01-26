package com.zebrarfid.demo.dto.login;

import lombok.Data;

/**
 * 登入请求
 * 携带用户名和密码
 */
@Data
public class LoginRequest {
    private String username;
    private String password;
}
