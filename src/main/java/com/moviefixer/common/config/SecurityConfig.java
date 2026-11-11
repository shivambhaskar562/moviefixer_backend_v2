package com.moviefixer.common.config;

import com.moviefixer.auth.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${app.cors.allowed-origins:${frontend.urls:${frontend.url}}}")
    private List<String> allowedOrigins;

    @Value("${app.cors.allowed-methods}")
    private List<String> allowedMethods;

    @Value("${app.cors.allowed-headers}")
    private List<String> allowedHeaders;

    @Value("${app.cors.allow-credentials}")
    private boolean allowCredentials;

    @Value("${app.cors.max-age}")
    private Long maxAge;

    @Value("${app.security.bcrypt.strength:12}")
    private int bcryptStrength;

    @Value("${app.security.password.pepper}")
    private String passwordPepper;

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PepperedPasswordEncoder(new BCryptPasswordEncoder(bcryptStrength), passwordPepper);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public Authentication & Verification endpoints
                        .requestMatchers("/api/auth/**").permitAll()

                        // Public User Registration
                        .requestMatchers(HttpMethod.POST, "/api/user/register").permitAll()

                        // Public Geographic reference endpoints
                        .requestMatchers(HttpMethod.GET, "/api/locations/**").permitAll()

                        // Public Movie Catalog & Metadata lookups
                        .requestMatchers(HttpMethod.GET, "/api/movies/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/genres/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/languages/**").permitAll()

                        // Public Theater, Screen & Seating Tier lookups
                        .requestMatchers(HttpMethod.GET, "/api/theaters/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/screens/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/seating-tiers/**").permitAll()

                        // Public Shows lookups
                        .requestMatchers(HttpMethod.GET, "/api/shows/**").permitAll()

                        // Bookings require authentication
                        .requestMatchers("/api/bookings/**").authenticated()

                        // Gate staff admission validation
                        .requestMatchers(HttpMethod.POST, "/api/tickets/validate-qr").hasAnyAuthority(
                                "ROLE_ADMIN", "ROLE_THEATER_MANAGER", "ROLE_THEATER_TICKET_STAFF", "ROLE_THEATER_VENDOR"
                        )
                        // Analytics & Multi-Vendor Dashboards
                        .requestMatchers("/api/analytics/platform/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/api/analytics/vendor/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_THEATER_VENDOR")
                        .requestMatchers("/api/analytics/shows/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_THEATER_VENDOR", "ROLE_THEATER_MANAGER")
                        .requestMatchers("/api/tickets/**").authenticated()

                        // Swagger / OpenAPI & Error endpoints
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/error"
                        ).permitAll()


                        // Authenticated user profile lookup
                        .requestMatchers("/api/user/me").authenticated()

                        // Admin user creation
                        .requestMatchers(HttpMethod.POST, "/api/user/create").hasAuthority("ROLE_ADMIN")

                        // Remaining requests permitted for current milestone compatibility
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        List<String> origins = (allowedOrigins != null)
                ? allowedOrigins.stream().map(String::trim).filter(s -> !s.isEmpty()).toList()
                : List.of();

        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods((allowedMethods != null) ? allowedMethods.stream().map(String::trim).toList() : List.of());
        configuration.setAllowedHeaders((allowedHeaders != null) ? allowedHeaders.stream().map(String::trim).toList() : List.of("*"));
        configuration.setAllowCredentials(allowCredentials);
        configuration.setMaxAge(maxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
