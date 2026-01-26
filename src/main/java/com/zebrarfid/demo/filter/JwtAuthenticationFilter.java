package com.zebrarfid.demo.filter;

import com.zebrarfid.demo.service.impl.login.UserDetailsServiceImpl;
import com.zebrarfid.demo.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 1. 获取请求头中的JWT令牌
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            // 无令牌，直接放行（后续Security会拦截未认证请求）
            filterChain.doFilter(request, response);
            return;
        }
        token = token.substring(7); // 去掉"Bearer "前缀

        // 2. 解析令牌，获取用户名
        String username = jwtUtil.getClaimsFromToken(token).getSubject();
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 3. 加载用户信息
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            // 4. 校验令牌有效性
            if (jwtUtil.validateToken(token, userDetails)) {
                // 5. 设置认证信息到Security上下文
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        // 放行
        filterChain.doFilter(request, response);
    }
}
