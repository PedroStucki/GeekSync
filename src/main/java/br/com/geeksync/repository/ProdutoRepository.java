package br.com.geeksync.repository;

import br.com.geeksync.domain.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    // Aqui usaremos os métodos padrão do JPA (save, findAll, findById) para o RF-02 [cite: 19]
}