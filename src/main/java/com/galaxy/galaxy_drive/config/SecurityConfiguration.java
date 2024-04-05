package com.galaxy.galaxy_drive.config;

import com.galaxy.galaxy_drive.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
    private final UserService userService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests((authz) -> authz
                        .requestMatchers("/login", "/registration").permitAll()
                        .requestMatchers("/css/**", "/picture/**", "/js/**").permitAll()
                        .anyRequest().authenticated())
                .formLogin(login -> login.loginPage("/login")
                        .defaultSuccessUrl("/", true))
                .oauth2Login(config -> config
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(oidcUserService())
                                .userService(oauth2UserService()))
                        .defaultSuccessUrl("/", true));
        return http.build();
    }

    private OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService()  {
        return userRequest -> {
            userService.createUserIfNotExist(userRequest);
            var userDetails = userService.loadUserByUsername(userRequest.getIdToken().getEmail());
            return new DefaultOidcUser(userDetails.getAuthorities(), userRequest.getIdToken());
        };

    }

    private OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {
        return userRequest -> {
            OAuth2User oauth2User = new DefaultOAuth2UserService().loadUser(userRequest);
            userService.createUserIfNotExist(oauth2User);
            return oauth2User;
        };
    }
}
