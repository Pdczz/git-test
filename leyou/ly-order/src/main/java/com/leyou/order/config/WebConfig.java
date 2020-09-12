package com.leyou.order.config;

import com.leyou.order.config.JwtProperties;
import com.leyou.order.inteceptor.UserInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private JwtProperties prop;

    @Bean
    public UserInterceptor getInteceptor(){
        return new UserInterceptor(prop);
    }
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(getInteceptor()).addPathPatterns("/**");

    }
}
