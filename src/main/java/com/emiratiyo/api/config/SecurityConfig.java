package com.emiratiyo.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Value("${spring.boot.admin.username:admin}")
        private String adminUsername;

        @Value("${spring.boot.admin.password:password}")
        private String adminPassword;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                return http
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                "/assets/**", "/actuator/**", "/instances/**",
                                                                "/api/**",
                                                                "/login", "/logout", "/*.js", "/*.css", "/*.ico",
                                                                "/*.png")
                                                .permitAll()
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .permitAll())
                                .logout(logout -> logout.logoutUrl("/logout"))
                                .httpBasic(Customizer.withDefaults())
                                .csrf(csrf -> csrf
                                                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                                                .ignoringRequestMatchers("/api/**", "/instances/**", "/actuator/**"))
                                .build();
        }

        @Bean
        public UserDetailsService userDetailsService() {
                UserDetails admin = User.withUsername(adminUsername)
                                .password("{noop}" + adminPassword)
                                .roles("ADMIN")
                                .build();
                return new InMemoryUserDetailsManager(admin);
        }
}