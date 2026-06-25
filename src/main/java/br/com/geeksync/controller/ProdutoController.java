package br.com.geeksync.controller;

import br.com.geeksync.domain.entity.Produto;
import br.com.geeksync.dto.ProdutoDTO;
import br.com.geeksync.service.ProdutoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST responsável pelos endpoints de Gestão de Produtos (RF-02).
 *
 * ROTAS DISPONÍVEIS:
 *   GET    /api/produtos       → lista todos os produtos
 *   GET    /api/produtos/{id}  → busca produto por ID
 *   POST   /api/produtos       → cadastra novo produto
 *   PUT    /api/produtos/{id}  → atualiza produto existente
 *
 * O campo "estoqueAbaixoDoMinimo" na resposta acende o alerta visual no frontend (RF-09).
 */
@RestController
@RequestMapping("/api/produtos")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ProdutoController {

    private final ProdutoService produtoService;

    /**
     * GET /api/produtos
     * Lista todos os produtos. O frontend usa "estoqueAbaixoDoMinimo"
     * para exibir o alerta de estoque baixo (RF-09).
     */
    @GetMapping
    public ResponseEntity<List<ProdutoDTO>> listarTodos() {
        List<ProdutoDTO> produtos = produtoService.listarTodos()
                .stream()
                .map(p -> converterParaDTO(p, produtoService.necessitaReposicao(p)))
                .toList();
        return ResponseEntity.ok(produtos);
    }

    /**
     * GET /api/produtos/{id}
     * Busca um produto específico pelo ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProdutoDTO> buscarPorId(@PathVariable Long id) {
        Produto produto = produtoService.buscarPorId(id);
        return ResponseEntity.ok(converterParaDTO(produto, produtoService.necessitaReposicao(produto)));
    }

    /**
     * POST /api/produtos
     * Cadastra um novo produto. O @Positive no DTO já rejeita preços inválidos (RF-02).
     */
    @PostMapping
    public ResponseEntity<ProdutoDTO> cadastrar(@Valid @RequestBody ProdutoDTO dto) {
        Produto salvo = produtoService.salvar(converterParaEntidade(dto));
        return ResponseEntity.status(201).body(converterParaDTO(salvo, produtoService.necessitaReposicao(salvo)));
    }

    /**
     * PUT /api/produtos/{id}
     * Atualiza os dados de um produto existente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProdutoDTO> atualizar(@PathVariable Long id, @Valid @RequestBody ProdutoDTO dto) {
        Produto existente = produtoService.buscarPorId(id);
        existente.setNome(dto.nome());
        existente.setDescricao(dto.descricao());
        existente.setPreco(dto.preco());
        existente.setQtdEstoque(dto.qtdEstoque());
        existente.setEstoqueMinimo(dto.estoqueMinimo() != null ? dto.estoqueMinimo() : 5);
        existente.setCategoria(dto.categoria());
        Produto atualizado = produtoService.salvar(existente);
        return ResponseEntity.ok(converterParaDTO(atualizado, produtoService.necessitaReposicao(atualizado)));
    }

    // ==========================================================================
    // Métodos de conversão entre Entidade e DTO
    // ==========================================================================

    private ProdutoDTO converterParaDTO(Produto produto, boolean estoqueAbaixo) {
        return new ProdutoDTO(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getPreco(),
                produto.getQtdEstoque(),
                produto.getEstoqueMinimo(),
                produto.getCategoria(),
                estoqueAbaixo
        );
    }

    private Produto converterParaEntidade(ProdutoDTO dto) {
        return Produto.builder()
                .nome(dto.nome())
                .descricao(dto.descricao())
                .preco(dto.preco())
                .qtdEstoque(dto.qtdEstoque())
                .estoqueMinimo(dto.estoqueMinimo() != null ? dto.estoqueMinimo() : 5)
                .categoria(dto.categoria())
                .build();
    }
}