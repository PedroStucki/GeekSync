package br.com.geeksync.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "clientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    private String telefone;
    private String endereco;

    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em")
    private LocalDateTime updatedEm;

    @PrePersist
    protected void onCreate() {
        criadoEm = LocalDateTime.now();
        updatedEm = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedEm = LocalDateTime.now();
    }
}