package br.com.geeksync.dto;

import br.com.geeksync.domain.enums.StatusVenda;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de SAÍDA retornado após POST /api/vendas e GET /api/vendas/{id}.
 * Nunca expõe a entidade JPA diretamente — apenas os dados necessários para o frontend.
 *
 * Exemplo de JSON retornado:
 * {
 *   "id": 42,
 *   "dataVenda": "2026-06-24T22:30:00",
 *   "nomeCliente": "João Silva",
 *   "usuarioResponsavel": "vendedor01",
 *   "itens": [...],
 *   "valorTotal": 270.00,
 *   "desconto": 30.00,
 *   "status": "ABERTA"
 * }
 */
public record VendaResponseDTO(

        Long id,

        LocalDateTime dataVenda,

        String nomeCliente,

        String usuarioResponsavel,

        List<ItemVendaDTO> itens,

        BigDecimal valorTotal,

        BigDecimal desconto,

        StatusVenda status
) {}