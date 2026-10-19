package com.changsha.zhongdian.common.web;

import com.changsha.zhongdian.auth.interceptor.AuthInterceptor;
import com.changsha.zhongdian.common.logging.RequestLoggingInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final RequestLoggingInterceptor requestLoggingInterceptor;
    private final AuthInterceptor authInterceptor;

    public WebMvcConfig(RequestLoggingInterceptor requestLoggingInterceptor, AuthInterceptor authInterceptor) {
        this.requestLoggingInterceptor = requestLoggingInterceptor;
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestLoggingInterceptor)
                .addPathPatterns("/api/**");

        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/health",
                        "/api/auth/login",
                        "/api/diagnostic/**",
                        "/api/stations/statistics",
                        "/api/stations/piles/statistics",
                        "/api/shareholders/statistics"
                );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*", "http://8.138.114.25:*", "http://8.163.118.2:*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
