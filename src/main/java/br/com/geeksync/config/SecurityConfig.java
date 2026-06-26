package br.com.geeksync.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

/**
 * Configuração central do Spring Security.
 *
 * Regras de acesso por perfil:
 * - ADMIN    → acesso total
 * - GERENTE  → cadastros + relatórios
 * - VENDEDOR → vendas + consultas
 *
 * Rota pública: POST /auth/login (não requer token)
 * Todas as demais rotas requerem JWT válido.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Desabilita CSRF — desnecessário para APIs REST stateless
                .csrf(AbstractHttpConfigurer::disable)

                // Configura CORS para aceitar requisições do frontend
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Stateless — não cria sessão HTTP (JWT cuida da autenticação)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Regras de autorização por rota e perfil
                .authorizeHttpRequests(auth -> auth

                        // Rota pública — login não requer token
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()

                        // Relatórios — apenas ADMIN e GERENTE
                        .requestMatchers("/api/relatorios/**")
                        .hasAnyRole("ADMIN", "GERENTE")

                        // Cadastro de clientes e produtos — ADMIN e GERENTE
                        .requestMatchers(HttpMethod.POST, "/api/clientes/**", "/api/produtos/**")
                        .hasAnyRole("ADMIN", "GERENTE")
                        .requestMatchers(HttpMethod.PUT, "/api/clientes/**", "/api/produtos/**")
                        .hasAnyRole("ADMIN", "GERENTE")
                        .requestMatchers(HttpMethod.DELETE, "/api/clientes/**", "/api/produtos/**")
                        .hasAnyRole("ADMIN", "GERENTE")

                        // Consultas de clientes e produtos — todos autenticados
                        .requestMatchers(HttpMethod.GET, "/api/clientes/**", "/api/produtos/**")
                        .hasAnyRole("ADMIN", "GERENTE", "VENDEDOR")

                        // Vendas — todos autenticados podem registrar e consultar
                        .requestMatchers("/api/vendas/**")
                        .hasAnyRole("ADMIN", "GERENTE", "VENDEDOR")

                        // Qualquer outra rota requer autenticação
                        .anyRequest().authenticated()
                )

                // Adiciona o filtro JWT antes do filtro padrão do Spring Security
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Configuração de CORS para permitir requisições do frontend.
     * Em produção, substituir "*" pelo domínio real.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            throw new UsernameNotFoundException("Use o endpoint /auth/login");
        };
    }
}