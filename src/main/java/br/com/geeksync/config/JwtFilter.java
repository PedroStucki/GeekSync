
package br.com.geeksync.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro JWT executado em CADA requisição HTTP.
 * Intercepta o header Authorization, valida o token e
 * autentica o usuário no contexto do Spring Security.
 *
 * Fluxo:
 * 1. Extrai o token do header "Authorization: Bearer <token>"
 * 2. Valida o token com JwtUtil
 * 3. Extrai username e perfil do token
 * 4. Registra a autenticação no SecurityContext
 * 5. Libera a requisição para o Controller
 */
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Verifica se o header existe e começa com "Bearer "
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtUtil.validarToken(token)) {
                String username = jwtUtil.extrairUsername(token);
                String perfil = jwtUtil.extrairPerfil(token);

                // Cria a autenticação com a role do perfil
                UsernamePasswordAuthenticationToken autenticacao =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + perfil))
                        );

                // Registra no contexto do Spring Security
                SecurityContextHolder.getContext().setAuthentication(autenticacao);
            }
        }

        // Continua a cadeia de filtros
        filterChain.doFilter(request, response);
    }
}