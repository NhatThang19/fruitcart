package com.vn.fruitcart.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

  private final CustomSuccessHandler customSuccessHandler;

  private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

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
  public HttpSessionEventPublisher httpSessionEventPublisher() {
    return new HttpSessionEventPublisher();
  }

  @Bean
  public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService());
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers(
                "/register",
                "/login",
                "/products",
                "/storage/**",
                "/admin/assets/**",
                "/shared/assets/**",
                "/client/assets/**",
                "/client/**",
                "/",
                "/error/**")
            .permitAll()
            .requestMatchers("/admin/**").hasRole("Admin")
            .anyRequest().authenticated())
        .formLogin(formLogin -> formLogin
            .loginPage("/login")
            .usernameParameter("email")
            .passwordParameter("password")
            .successHandler(customSuccessHandler)
            .failureHandler(customAuthenticationFailureHandler)
            .permitAll())
        .logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/login?logout")
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID", "remember-me")
            .permitAll())
        .rememberMe(rememberMe -> rememberMe
            .key("uniqueAndSecret")
            .tokenValiditySeconds(30 * 24 * 60 * 60)
            .rememberMeParameter("remember-me")
            .userDetailsService(userDetailsService()))
        .sessionManagement(sessionManagement -> sessionManagement
            .sessionFixation().migrateSession()
            .maximumSessions(1)
            .sessionRegistry(sessionRegistry())
            .expiredUrl("/login?sessionExpired"));

    return http.build();
  }

}