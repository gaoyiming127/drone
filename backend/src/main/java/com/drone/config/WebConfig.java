package com.drone.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Web 跨域配置。
 *
 * <p>前后端分离部署时前端与后端不同源，这里对 /api/** 放行所有来源、请求头与请求方法，
 * 并允许携带凭证（Authorization 请求头）。</p>
 */
@Configuration
public class WebConfig {

    /**
     * 注册跨域过滤器，仅作用于 /api/** 路径。
     *
     * @return 跨域过滤器
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return new CorsFilter(source);
    }
}
