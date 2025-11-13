package br.mack.estagio;

import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private SecurityFilter securityFilter;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration configuration = new CorsConfiguration();
                    configuration.addAllowedOrigin("http://localhost:3000"); // Porta padrão do Next.js
                    configuration.addAllowedMethod("*");
                    configuration.addAllowedHeader("*");
                    return configuration;
                }))
                .csrf(AbstractHttpConfigurer::disable) // 1. Desabilita CSRF
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // API REST não guarda estado
                .authorizeHttpRequests(authorize -> authorize
                        // Endpoints públicos para registro e login
                        .requestMatchers("/auth/login", "/estudantes", "/empresas/registrar").permitAll()
                        // Endpoints públicos para visualização de vagas (não requer login)
                        .requestMatchers(HttpMethod.GET, "/vagas", "/vagas/{id}").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll() // Libera o Swagger

                        // Endpoints para ÁREAS DE INTERESSE (apenas ADMIN)
                        .requestMatchers("/areas-interesse/**").hasRole("ADMIN")

                        // Endpoints para EMPRESAS
                        .requestMatchers(HttpMethod.POST, "/vagas").hasRole("EMPRESA")
                        .requestMatchers(HttpMethod.PUT, "/vagas/**").hasRole("EMPRESA")
                        .requestMatchers(HttpMethod.PATCH, "/vagas/**").hasRole("EMPRESA")
                        .requestMatchers(HttpMethod.DELETE, "/vagas/**").hasRole("EMPRESA")
                        .requestMatchers("/empresas/me/**").hasRole("EMPRESA")

                        // Endpoints para ESTUDANTES
                        .requestMatchers("/inscricoes/**").hasRole("ESTUDANTE")
                        .requestMatchers(HttpMethod.GET, "/estudantes/me/vagas-recomendadas").hasRole("ESTUDANTE")
                        .requestMatchers(HttpMethod.PUT, "/estudantes/{id}").hasRole("ESTUDANTE")

                        // Endpoints para ADMIN
                        .requestMatchers("/dashboard/**", "/usuarios/**", "/empresas", "/empresas/{id}", "/estudantes", "/estudantes/{id}", "/admins/**").hasRole("ADMIN")
                        // Todas as outras rotas exigem autenticação
                        .anyRequest().authenticated()
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}