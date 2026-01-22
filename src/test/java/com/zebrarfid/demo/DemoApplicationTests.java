package com.zebrarfid.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
class DemoApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    public static void main(String[] args) {
        // 生成密码123456的BCrypt加密串
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encryptPassword = encoder.encode("123456");
        System.out.println("加密后的密码：" + encryptPassword);
    }

}
