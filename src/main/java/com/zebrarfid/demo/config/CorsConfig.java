package com.zebrarfid.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置类（解决前端请求后端接口的CORS限制）
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        // 1. 构建CORS配置对象，配置跨域规则
        CorsConfiguration config = new CorsConfiguration();

        // 允许前端的域名（开发阶段指定具体域名更规范，*表示允许所有）
        // 如果你前端是8081端口，直接写http://localhost:8081，生产环境替换为实际域名
        config.addAllowedOrigin("http://localhost:8081");

        // 允许携带Cookie（JWT认证不需要Cookie可保留，不影响）
        config.setAllowCredentials(true);

        // 允许所有请求头（包含Authorization、Content-Type等）
        config.addAllowedHeader(CorsConfiguration.ALL);

        // 允许所有请求方法（GET/POST/PUT/DELETE等）
        config.addAllowedMethod(CorsConfiguration.ALL);

        // 预检请求的有效期（3600秒=1小时，避免频繁发预检请求）
        config.setMaxAge(3600L);

        // 2. 配置哪些接口生效（/**表示所有接口）
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        // 3. 返回CORS过滤器，交给Spring容器管理
        return new CorsFilter(source);
    }
}
