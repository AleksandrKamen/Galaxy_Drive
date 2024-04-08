package com.galaxy.galaxy_drive.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
@TestConfiguration
public class TestSecurityConfiguration {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf((csrf) -> csrf.disable())
                .authorizeHttpRequests((authz) -> authz
                        .requestMatchers("/login", "/registration").permitAll()
                        .requestMatchers("/css/**", "/picture/**", "/js/**").permitAll()
                        .anyRequest().authenticated())
                .formLogin(login -> login.loginPage("/login")
                        .defaultSuccessUrl("/", true));
//                .oauth2Login(config -> config
//                        .loginPage("/login")
//                        .userInfoEndpoint(userInfo -> userInfo
//                                .oidcUserService(oidcUserService())
//                                .userService(oauth2UserService()))
//                        .defaultSuccessUrl("/", true));
        return http.build();
    }
}
