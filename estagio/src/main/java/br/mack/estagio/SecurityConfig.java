package br.mack.estagio;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
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
                .csrf(csrf -> csrf.disable()) // 1. Desabilita CSRF
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // API REST não guarda estado
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, "/usuarios", "/usuarios/login").permitAll() // 2. Libera os endpoints de criar usuário e login
                        .requestMatchers(HttpMethod.POST, "/estudantes").permitAll() // Libera o registro de novos estudantes
                        .requestMatchers(HttpMethod.POST, "/empresas/registrar").permitAll() // Libera o registro de novas empresas
                        .requestMatchers(HttpMethod.GET, "/vagas", "/vagas/**").permitAll() // Libera a visualização de vagas para todos
                        
                        // Regras de Acesso por Perfil
                        .requestMatchers("/admins/**").hasRole("ADMIN")
                        .requestMatchers("/areas-interesse/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/empresas", "/empresas/{id}").hasRole("ADMIN")

                        // Regras para Empresa
                        .requestMatchers("/empresas/me/**").hasRole("EMPRESA")
                        .requestMatchers(HttpMethod.POST, "/vagas").hasRole("EMPRESA")
                        .requestMatchers(HttpMethod.PUT, "/vagas/**").hasRole("EMPRESA")
                        .requestMatchers(HttpMethod.PATCH, "/vagas/**").hasRole("EMPRESA")
                        .requestMatchers(HttpMethod.GET, "/vagas/{id}/candidatos").hasRole("EMPRESA")
                        .requestMatchers(HttpMethod.DELETE, "/vagas/**").hasRole("EMPRESA")

                        .requestMatchers("/inscricoes/**").hasRole("ESTUDANTE")
                        .requestMatchers(HttpMethod.GET, "/estudantes/me/vagas-recomendadas").hasRole("ESTUDANTE")

                        .anyRequest().authenticated() // 3. Exige autenticação para todas as outras requisições
                ).build();
    }
}