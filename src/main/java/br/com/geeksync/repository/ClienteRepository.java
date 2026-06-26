package br.com.geeksync.repository;

import br.com.geeksync.domain.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByCpf(String cpf); // Exigência do RF-01 [cite: 19, 36]
    boolean existsByCpf(String cpf);         // Para validação de duplicidade [cite: 36]
    boolean existsByEmail(String email);     // Para validação de e-mail único [cite: 68]
}