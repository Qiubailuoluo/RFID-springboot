package com.zebrarfid.demo.dto;

import lombok.Data;

/**
 * 注册请求DTO
 */
@Data
public class RegisterRequest {
    /**
     * 用户名（唯一）
     */
    private String username;

    /**
     * 密码（明文，后端加密存储）
     */
    private String password;

    /**
     * 昵称
     */
    private String nickname;
}
