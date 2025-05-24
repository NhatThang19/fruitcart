package com.vn.fruitcart.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
  @Autowired
  private CustomSuccessHandler customSuccessHandler;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public UserDetailsService userDetailsService() {
    return new UserDetailsCustom();
  }

  @Bean
  public SessionRegistry sessionRegistry() {
    return new SessionRegistryImpl();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers(
                "/register",
                "/login",
                "/admin/assets/**",
                "/shared/assets/**",
                "/client/assets/**",
                "/client/**",
                "/",
                "/error/**")
            .permitAll()
            .requestMatchers("/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated())
        .formLogin(formLogin -> formLogin
            .loginPage("/login")
            .usernameParameter("email")
            .passwordParameter("password")
            .successHandler(customSuccessHandler)
            .failureUrl("/login?error=true")
            .permitAll())
        .logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/login?logout=true")
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID", "remember-me")
            .permitAll())
        .rememberMe(rememberMe -> rememberMe
            .key("myUniqueKey")
            .tokenValiditySeconds(86400)
            .userDetailsService(userDetailsService()))
        .sessionManagement(sessionManagement -> sessionManagement
            .sessionFixation().migrateSession()
            .maximumSessions(1)
            .sessionRegistry(sessionRegistry())
            .expiredUrl("/login?sessionExpired=true"))
        .exceptionHandling(exceptionHandling -> exceptionHandling
            .accessDeniedPage("/error/403"));
    return http.build();
  }

}