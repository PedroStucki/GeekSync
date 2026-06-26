package br.com.geeksync.controller;

import br.com.geeksync.config.JwtUtil;
import br.com.geeksync.domain.entity.Usuario;
import br.com.geeksync.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credenciais) {
        String username = credenciais.get("username");
        String senha = credenciais.get("password");

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        if (!passwordEncoder.matches(senha, usuario.getSenha())) {
            return ResponseEntity.status(401).body(Map.of(
                    "erro", "CREDENCIAIS_INVALIDAS",
                    "mensagem", "Username ou senha incorretos."
            ));
        }

        String token = jwtUtil.gerarToken(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getPerfil().name()
        );

        Map<String, Object> dadosUsuario = new LinkedHashMap<>();
        dadosUsuario.put("id", usuario.getId());
        dadosUsuario.put("nome", usuario.getUsername());
        dadosUsuario.put("perfil", usuario.getPerfil().name());

        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("token", token);
        resposta.put("usuario", dadosUsuario);

        return ResponseEntity.ok(resposta);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        return ResponseEntity.ok(Map.of("mensagem", "Logout realizado com sucesso."));
    }
}