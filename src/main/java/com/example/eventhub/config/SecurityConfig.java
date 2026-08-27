package com.example.eventhub.config;

import com.example.eventhub.auth.details.CustomUserDetailsService;
import com.example.eventhub.auth.handling.MyAccessDeniedHandler;
import com.example.eventhub.auth.handling.MyAuthenticationEntryPoint;
import com.example.eventhub.auth.jwt.JwtTokenFilter;
import com.example.eventhub.auth.oauth.MyFailureHandler;
import com.example.eventhub.auth.oauth.MyOAuth2UserService;
import com.example.eventhub.auth.oauth.MyOidcUserService;
import com.example.eventhub.auth.oauth.MySuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true,securedEnabled = true,jsr250Enabled = true)
public class SecurityConfig {

    private final MyAccessDeniedHandler accessDeniedHandler;
    private final MyAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtTokenFilter jwtTokenFilter;
    private final MySuccessHandler successHandler;
    private final MyFailureHandler failureHandler;
    private final MyOAuth2UserService oAuth2UserService;
    private final MyOidcUserService oidcUserService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http){
        http.authorizeHttpRequests(auth->auth
                .requestMatchers(HttpMethod.OPTIONS,"/**").permitAll()
                .requestMatchers("/api/v1/auth/register").permitAll()
                .requestMatchers("/api/v1/auth/refresh").permitAll()
                .requestMatchers("/api/v1/auth/exchange").permitAll()
                .requestMatchers("/api/v1/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET,"/api/v1/events").permitAll()
                .requestMatchers(HttpMethod.GET,"/api/v1/events/{id}").permitAll()
                .requestMatchers(HttpMethod.GET,"/api/v1/events/{eventId}/reviews").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/swagger-ui.html").permitAll()
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.POST,"/api/v1/events").hasAllRoles("ADMIN","ORGANIZER")
                .requestMatchers(HttpMethod.PATCH,"/api/v1/events/{id}/complete").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE,"/api/v1/events/{id}").hasRole("ADMIN")
                .anyRequest().authenticated()
        )
                .exceptionHandling(handling->handling
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .csrf(csrf->csrf.disable())
                .sessionManagement(session->session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(header->header
                        .frameOptions(frame->frame.deny())
                        .contentTypeOptions(Customizer.withDefaults())
                        .httpStrictTransportSecurity(hsts->hsts
                                .includeSubDomains(true)
                                .preload(true)
                                .maxAgeInSeconds(31536000))
                        )
                .cors(cors->cors.configurationSource(corsConfigurationSource()))
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oauth->oauth
                        .successHandler(successHandler)
                        .failureHandler(failureHandler)
                        .userInfoEndpoint(info->info
                                .userService(oAuth2UserService)
                                .oidcUserService(oidcUserService)));

        return http.build();
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration){
        return configuration.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(CustomUserDetailsService userDetailsService,PasswordEncoder passwordEncoder){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedHeaders(List.of("Authorization","X-Request-ID","Content-Type"));
        configuration.setExposedHeaders(List.of("Location"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**",configuration);

        return source;
    }
}
