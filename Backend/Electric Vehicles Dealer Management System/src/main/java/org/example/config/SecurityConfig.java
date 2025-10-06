package org.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http.csrf().disable()
//                .authorizeHttpRequests()
//                // public paths
//                .requestMatchers("/login", "/css/**", "/js/**", "/images/**", "/api/auth/**").permitAll()
//                // everything else requires authentication
//                .anyRequest().permitAll()
//                .and()
//                .formLogin()
//                .loginPage("/login")              // custom login page
//                .defaultSuccessUrl("/main", true) // after login, go to Main Page
//                .permitAll()
//                .and()
//                .logout()
//                .logoutUrl("/logout")
//                .logoutSuccessUrl("/login?logout")
//                .permitAll();
//
//        return http.build();
//    }



    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeHttpRequests()
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().permitAll();
        return http.build();
    }
}
