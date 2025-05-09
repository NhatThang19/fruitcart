package com.vn.fruitcart.config;

import com.vn.fruitcart.service.UserService;
import java.util.List;

import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfiguration {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationSuccessHandler customSuccessHandler(UserService userService) {
    return new CustomSuccessHandler(userService);
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("http://localhost:8080"));
    config.setAllowedMethods(List.of("GET", "POST"));
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }

  @Bean
  public SessionRegistry sessionRegistry() {
    return new SessionRegistryImpl();
  }

  @Bean
  public ServletListenerRegistrationBean<HttpSessionEventPublisher> httpSessionEventPublisher() {
    return new ServletListenerRegistrationBean<>(new HttpSessionEventPublisher());
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http, UserService userService,
      UserDetailsCustom userDetailsCustom) throws Exception {
    String[] whiteList = {
        "/", "/login", "/register",
        "/storage/**", "/admin/assets/**", "/client/assets/**", "/shared/assets/**",
        "/api/users"
    };

    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(whiteList).permitAll()
            .requestMatchers("/admin").hasRole("ADMIN")
            .anyRequest().authenticated())
        .formLogin(form -> form
            .loginPage("/login")
            .successHandler(customSuccessHandler(userService))
            .failureHandler((request, response, exception) -> {
              if (exception.getMessage().contains("User is disabled")) {
                response.sendRedirect("/login?banned");
              } else if (exception.getMessage().contains("Bad credentials")) {
                response.sendRedirect("/login?error");
              } else if (exception.getMessage().contains("Session expired")) {
                response.sendRedirect("/login?expired");
              }
            })
            .permitAll())
        .rememberMe(remember -> remember
            .key("uniqueAndSecret")
            .tokenValiditySeconds(30 * 24 * 60 * 60)
            .rememberMeParameter("remember-me")
            .userDetailsService(userDetailsCustom))
        .logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessHandler((request, response, authentication) -> {
              response.sendRedirect("/login?logout");
            })
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID", "remember-me")
            .permitAll())
        .sessionManagement((sessionManagement) -> sessionManagement
            .sessionFixation().migrateSession()
            .maximumSessions(1)
            .maxSessionsPreventsLogin(true)
            .expiredUrl("/login?expired")
            .sessionRegistry(sessionRegistry()))
        .exceptionHandling(exception -> exception
            .authenticationEntryPoint((request, response, authException) -> {
              request.getSession().setAttribute("errorMessage", "Vui lòng đăng nhập để tiếp tục.");
              response.sendRedirect("/login");
            })
            .accessDeniedHandler((request, response, accessDeniedException) -> {
              response.sendRedirect("/access-denied");
            }));

    return http.build();
  }

}
