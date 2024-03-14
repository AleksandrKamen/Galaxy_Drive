package com.galaxy.galaxy_drive.config;

import com.galaxy.galaxy_drive.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
    private final UserService userService;
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((authz) -> authz
                        .requestMatchers("/login", "/registration").permitAll()
                        .requestMatchers("/css/**", "/picture/**").permitAll()
                        .anyRequest().authenticated())
                .formLogin(login -> login.loginPage("/login")
                        .defaultSuccessUrl("/", true))
                .oauth2Login(config -> config
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo.oidcUserService(oAuth2UserService()))
                        .defaultSuccessUrl("/", true));

        return http.build();
    }

    private OAuth2UserService<OidcUserRequest, OidcUser> oAuth2UserService(){
        return userRequest -> {
            var email = userRequest.getIdToken().getEmail();
            var name = userRequest.getIdToken().getGivenName();
            var familyName = userRequest.getIdToken().getFamilyName();
            userService.createUserIfNotExist(email,name, familyName);
            var userDetails = userService.loadUserByUsername(email);
            return new DefaultOidcUser(userDetails.getAuthorities(), userRequest.getIdToken());
        };

    }


}
