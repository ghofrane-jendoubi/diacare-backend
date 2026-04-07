package tn.esprit.spring.diacarebackend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.*;

import tn.esprit.spring.diacarebackend.services.CustomUserDetailsService;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // ✅ Tous les endpoints publics
                        .requestMatchers("/api/admins/signup").permitAll()
                        .requestMatchers("/api/admins/activate").permitAll()
                        .requestMatchers("/api/admins/login").permitAll()
                        .requestMatchers("/api/doctors/signup").permitAll()
                        .requestMatchers("/api/doctors/activate").permitAll()
                        .requestMatchers("/api/doctors/login").permitAll()
                        .requestMatchers("/api/nutritionists/signup").permitAll()
                        .requestMatchers("/api/nutritionists/activate").permitAll()
                        .requestMatchers("/api/nutritionists/login").permitAll()
                        .requestMatchers("/api/patients/signup").permitAll()
                        .requestMatchers("/api/patients/activate").permitAll()
                        .requestMatchers("/api/patients/login").permitAll()
                        .requestMatchers("/api/doctors/all").permitAll()
                        .requestMatchers("/api/doctors/pending").permitAll()
                        .requestMatchers("/api/doctors/**").permitAll()
                        .requestMatchers("/api/nutritionists/all").permitAll()
                        .requestMatchers("/api/nutritionists/pending").permitAll()
                        .requestMatchers("/api/nutritionists/**").permitAll()
                        .requestMatchers("/api/patients/all").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        // ✅ Servir les images des certificats
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/api/patients/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        // Tout le reste nécessite authentification
                        .anyRequest().permitAll() // ← mets authenticated() en production
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("*")); // 🔥 IMPORTANT
        config.setAllowedHeaders(List.of("*")); // 🔥 IMPORTANT
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}