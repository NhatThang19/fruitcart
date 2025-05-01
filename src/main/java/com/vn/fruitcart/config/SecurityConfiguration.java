package com.vn.fruitcart.config;

import com.vn.fruitcart.service.UserService;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
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
  public SecurityFilterChain filterChain(HttpSecurity http, UserService userService,
      UserDetailsCustom userDetailsCustom) throws Exception {
    String[] whiteList = {
        "/", "/login", "/register",
        "/storage/**", "/admin/assets/**", "/client/assets/**",
    };

    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(whiteList).permitAll()
            .anyRequest().authenticated())
        .formLogin(form -> form
            .loginPage("/login")
            .successHandler(customSuccessHandler(userService))
            .failureHandler((request, response, exception) -> {
              String errorMessage;
              if (exception.getMessage().contains("User is disabled")) {
                errorMessage = "Tài khoản của bạn đã bị khóa.";
              } else if (exception.getMessage().contains("Bad credentials")) {
                errorMessage = "Tên đăng nhập hoặc mật khẩu không đúng!";
              } else {
                errorMessage = "Đăng nhập thất bại. Vui lòng thử lại.";
              }
              request.getSession().setAttribute("errorMessage", errorMessage);
              response.sendRedirect("/login");
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
              request.getSession().setAttribute("logoutMessage", "Đăng xuất thành công!");
              response.sendRedirect("/login");
            })
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID", "remember-me")
            .permitAll())
        .sessionManagement((sessionManagement) -> sessionManagement
            .invalidSessionUrl("/logout?expired")
            .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            .maximumSessions(1)
            .expiredUrl("/login?expired=true")
            .maxSessionsPreventsLogin(false))
        .exceptionHandling(exception -> exception
            .authenticationEntryPoint((request, response, authException) -> {
              request.getSession().setAttribute("errorMessage", "Vui lòng đăng nhập để tiếp tục.");
              response.sendRedirect("/login");
            })
            .accessDeniedHandler((request, response, accessDeniedException) -> {
              request.getSession().setAttribute("errorMessage", "Bạn không có quyền truy cập.");
              response.sendRedirect("/access-denied");
            }));

    return http.build();
  }

}
