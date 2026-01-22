package com.zebrarfid.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zebrarfid.demo.entity.User;
import com.zebrarfid.demo.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Spring Security核心：加载用户信息的服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor // 构造器注入（替代@Autowired）
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;

    /**
     * 根据用户名加载用户信息（Spring Security会自动调用这个方法）
     * @param username 前端传入的用户名
     * @return 封装后的UserDetails对象（Spring Security识别的用户信息）
     * @throws UsernameNotFoundException 用户名不存在时抛出
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 从数据库查询用户（根据用户名）
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username) // 条件：用户名相等
                .eq(User::getStatus, 1); // 条件：用户状态为正常（1）

        User user = userMapper.selectOne(queryWrapper);

        // 2. 如果用户不存在，抛出异常（Spring Security会捕获并返回认证失败）
        if (user == null) {
            log.error("用户名{}不存在", username);
            throw new UsernameNotFoundException("用户名或密码错误");
        }

        // 3. 封装成Spring Security的UserDetails对象（核心）
        // 参数说明：用户名、密码、权限列表（这里先给空列表，后续可扩展角色权限）
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(), // 数据库中加密后的密码，Spring Security会自动用BCrypt校验
                Collections.emptyList() // 权限列表，暂时为空，后续可加如["ROLE_ADMIN"]
        );
    }
}
