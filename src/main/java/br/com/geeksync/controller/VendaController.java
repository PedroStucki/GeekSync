package br.com.geeksync.controller;

import br.com.geeksync.domain.entity.ItemVenda;
import br.com.geeksync.domain.entity.Produto;
import br.com.geeksync.domain.entity.Venda;
import br.com.geeksync.dto.ItemVendaDTO;
import br.com.geeksync.dto.VendaRequestDTO;
import br.com.geeksync.dto.VendaResponseDTO;
import br.com.geeksync.service.VendaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST responsável pelos endpoints de Registro de Vendas (RF-03, RF-04, RF-10).
 *
 * ROTAS DISPONÍVEIS:
 *   POST   /api/vendas                → registra nova venda (PDV)
 *   GET    /api/vendas                → lista todas as vendas
 *   GET    /api/vendas/{id}           → busca venda por ID
 *   PATCH  /api/vendas/{id}/cancelar  → cancela venda com status ABERTA
 */
@RestController
@RequestMapping("/api/vendas")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class VendaController {

    private final VendaService vendaService;

    /**
     * POST /api/vendas
     * Registra uma nova venda.
     * O desconto de 10% em mangás/livros acima de R$150 é aplicado
     * automaticamente pelo VendaService (RF-04).
     */
    @PostMapping
    public ResponseEntity<VendaResponseDTO> realizarVenda(@Valid @RequestBody VendaRequestDTO request) {
        Venda venda = construirVendaDaRequisicao(request);
        Venda salva = vendaService.realizarVenda(request.clienteId(), request.usuarioId(), venda);
        return ResponseEntity.status(201).body(converterParaResponseDTO(salva));
    }

    /**
     * GET /api/vendas
     * Lista todas as vendas registradas.
     */
    @GetMapping
    public ResponseEntity<List<VendaResponseDTO>> listarTodas() {
        List<VendaResponseDTO> vendas = vendaService.listarTodas()
                .stream()
                .map(this::converterParaResponseDTO)
                .toList();
        return ResponseEntity.ok(vendas);
    }

    /**
     * GET /api/vendas/{id}
     * Busca uma venda específica com todos os seus itens.
     */
    @GetMapping("/{id}")
    public ResponseEntity<VendaResponseDTO> buscarPorId(@PathVariable Long id) {
        Venda venda = vendaService.buscarPorId(id);
        return ResponseEntity.ok(converterParaResponseDTO(venda));
    }

    /**
     * PATCH /api/vendas/{id}/cancelar
     * Cancela uma venda. Só permitido se status for ABERTA (RF-10).
     * O estoque dos produtos é revertido automaticamente pelo VendaService.
     */
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelarVenda(@PathVariable Long id) {
        vendaService.cancelarVenda(id);
        return ResponseEntity.noContent().build();
    }

    // ==========================================================================
    // Métodos de conversão entre Entidade e DTO
    // ==========================================================================

    private Venda construirVendaDaRequisicao(VendaRequestDTO request) {
        List<ItemVenda> itens = request.itens().stream()
                .map(itemDTO -> ItemVenda.builder()
                        .produto(Produto.builder().id(itemDTO.produtoId()).build())
                        .quantidade(itemDTO.quantidade())
                        .build())
                .toList();

        Venda venda = Venda.builder().build();
        venda.setItens(itens);
        return venda;
    }

    private VendaResponseDTO converterParaResponseDTO(Venda venda) {
        List<ItemVendaDTO> itensDTO = venda.getItens().stream()
                .map(item -> new ItemVendaDTO(
                        item.getId(),
                        item.getProduto().getId(),
                        item.getProduto().getNome(),
                        item.getQuantidade(),
                        item.getPrecoUnitario(),
                        item.getSubtotal()
                ))
                .toList();

        return new VendaResponseDTO(
                venda.getId(),
                venda.getDataVenda(),
                venda.getCliente().getNome(),
                venda.getUsuario().getUsername(),
                itensDTO,
                venda.getValorTotal(),
                venda.getDesconto(),
                venda.getStatus()
        );
    }
}