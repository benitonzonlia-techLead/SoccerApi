package com.bnz.soccer.controllers.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile({"dev", "test"})
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**", "/actuator/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/teams", "/api/teams/filter").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/teams").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/teams/**").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/teams/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/teams/**").authenticated()

                        .anyRequest().denyAll()
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
