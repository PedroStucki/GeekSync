package br.com.geeksync.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * DTO auxiliar que representa um item dentro de uma VendaRequestDTO.
 * O frontend envia o id do produto e a quantidade desejada.
 * O preço unitário e o subtotal são calculados no backend (VendaService).
 */
public record ItemVendaDTO(

        Long id, // Presente apenas na resposta

        @NotNull(message = "O ID do produto é obrigatório.")
        Long produtoId,

        String nomeProduto, // Presente apenas na resposta

        @NotNull(message = "A quantidade é obrigatória.")
        @Min(value = 1, message = "A quantidade mínima por item é 1.")
        Integer quantidade,

        BigDecimal precoUnitario, // Presente apenas na resposta

        BigDecimal subtotal // Presente apenas na resposta
) {}