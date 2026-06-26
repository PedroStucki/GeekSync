package br.com.geeksync.controller;

import br.com.geeksync.domain.entity.Cliente;
import br.com.geeksync.dto.ClienteDTO;
import br.com.geeksync.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST responsável pelos endpoints de Gestão de Clientes (RF-01).
 *
 * ROTAS DISPONÍVEIS:
 *   GET    /api/clientes       → lista todos os clientes
 *   GET    /api/clientes/{id}  → busca cliente por ID
 *   POST   /api/clientes       → cadastra novo cliente
 *   PUT    /api/clientes/{id}  → atualiza cliente existente
 *   DELETE /api/clientes/{id}  → remove cliente
 */
@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    /**
     * GET /api/clientes
     * Lista todos os clientes cadastrados.
     */
    @GetMapping
    public ResponseEntity<List<ClienteDTO>> listarTodos() {
        List<ClienteDTO> clientes = clienteService.listarTodos()
                .stream()
                .map(this::converterParaDTO)
                .toList();
        return ResponseEntity.ok(clientes);
    }

    /**
     * GET /api/clientes/{id}
     * Busca um cliente específico pelo ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClienteDTO> buscarPorId(@PathVariable Long id) {
        Cliente cliente = clienteService.buscarPorId(id);
        return ResponseEntity.ok(converterParaDTO(cliente));
    }

    /**
     * POST /api/clientes
     * Cadastra um novo cliente. O @Valid dispara as validações do ClienteDTO.
     */
    @PostMapping
    public ResponseEntity<ClienteDTO> cadastrar(@Valid @RequestBody ClienteDTO dto) {
        Cliente salvo = clienteService.salvar(converterParaEntidade(dto));
        return ResponseEntity.status(201).body(converterParaDTO(salvo));
    }

    /**
     * PUT /api/clientes/{id}
     * Atualiza os dados de um cliente existente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ClienteDTO> atualizar(@PathVariable Long id, @Valid @RequestBody ClienteDTO dto) {
        Cliente existente = clienteService.buscarPorId(id);
        existente.setNome(dto.nome());
        existente.setCpf(dto.cpf());
        existente.setEmail(dto.email());
        existente.setTelefone(dto.telefone());
        existente.setEndereco(dto.endereco());
        Cliente atualizado = clienteService.salvar(existente);
        return ResponseEntity.ok(converterParaDTO(atualizado));
    }

    /**
     * DELETE /api/clientes/{id}
     * Remove um cliente. O banco barra a deleção via constraint RESTRICT
     * se ele possuir vendas (RF-01).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        clienteService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    // ==========================================================================
    // Métodos de conversão entre Entidade e DTO
    // ==========================================================================

    private ClienteDTO converterParaDTO(Cliente cliente) {
        return new ClienteDTO(
                cliente.getId(),
                cliente.getNome(),
                cliente.getCpf(),
                cliente.getEmail(),
                cliente.getTelefone(),
                cliente.getEndereco()
        );
    }

    private Cliente converterParaEntidade(ClienteDTO dto) {
        return Cliente.builder()
                .nome(dto.nome())
                .cpf(dto.cpf())
                .email(dto.email())
                .telefone(dto.telefone())
                .endereco(dto.endereco())
                .build();
    }
}