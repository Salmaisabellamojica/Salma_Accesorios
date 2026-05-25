package com.salma.salma_accesorios.config;

import com.salma.salma_accesorios.security.JwtAuthenticationFilter;
import com.salma.salma_accesorios.security.CustomUserDetailsService;
import com.salma.salma_accesorios.security.JwtService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ObjectProvider<JwtAuthenticationFilter> jwtFilterProvider,
            ObjectProvider<LoginSuccessHandler> loginSuccessHandlerProvider,
            ObjectProvider<GoogleOAuth2SuccessHandler> googleOAuth2SuccessHandlerProvider) throws Exception {
        AuthenticationSuccessHandler resolvedSuccessHandler = loginSuccessHandlerProvider.getIfAvailable();
        AuthenticationSuccessHandler successHandler = resolvedSuccessHandler != null
                ? resolvedSuccessHandler
                : (request, response, authentication) -> response.sendRedirect("/");
        AuthenticationSuccessHandler resolvedGoogleSuccessHandler = googleOAuth2SuccessHandlerProvider.getIfAvailable();
        AuthenticationSuccessHandler googleSuccessHandler = resolvedGoogleSuccessHandler != null
                ? resolvedGoogleSuccessHandler
                : successHandler;

        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/registro", "/catalogo", "/css/**", "/js/**", "/img/**", "/uploads/**").permitAll()
                        .requestMatchers("/registro/google/completar", "/oauth2/**", "/login/oauth2/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/producto/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/producto/*/resenas").hasRole("CLIENTE")
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").hasRole("ADMIN")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/carrito", "/carrito/**", "/checkout", "/checkout/**", "/pago", "/pago/**", "/favoritos", "/favoritos/**", "/pedidos", "/pedidos/**", "/devoluciones", "/devoluciones/**", "/resenas", "/resenas/**").hasRole("CLIENTE")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(successHandler)
                        .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .successHandler(googleSuccessHandler)
                )
                .logout(logout -> logout
                        .deleteCookies("JSESSIONID")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .httpBasic(AbstractHttpConfigurer::disable);

        JwtAuthenticationFilter jwtAuthenticationFilter = jwtFilterProvider.getIfAvailable();
        if (jwtAuthenticationFilter != null) {
            http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        }

        return http.build();
    }

    @Bean
    @ConditionalOnBean({JwtService.class, CustomUserDetailsService.class})
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        return new JwtAuthenticationFilter(jwtService, userDetailsService);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
