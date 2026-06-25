package br.com.geeksync.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * DTO de ENTRADA para o endpoint POST /api/vendas.
 * É o "carrinho de compras" que o frontend (PDV) envia.
 *
 * Exemplo de JSON esperado:
 * {
 *   "clienteId": 1,
 *   "usuarioId": 2,
 *   "itens": [
 *     { "produtoId": 5, "quantidade": 2 },
 *     { "produtoId": 8, "quantidade": 1 }
 *   ]
 * }
 */
public record VendaRequestDTO(

        @NotNull(message = "O ID do cliente é obrigatório.")
        Long clienteId,

        @NotNull(message = "O ID do usuário responsável é obrigatório.")
        Long usuarioId,

        @NotEmpty(message = "Uma venda deve conter pelo menos 1 item.")
        @Valid
        List<ItemVendaDTO> itens
) {}