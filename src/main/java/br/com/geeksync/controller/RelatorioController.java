package br.com.geeksync.controller;

import br.com.geeksync.domain.entity.ItemVenda;
import br.com.geeksync.domain.entity.Venda;
import br.com.geeksync.dto.ItemVendaDTO;
import br.com.geeksync.dto.VendaResponseDTO;
import br.com.geeksync.service.VendaService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/relatorios")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class RelatorioController {

    private final VendaService vendaService;

    /**
     * GET /api/relatorios/por-periodo?inicio=2026-06-01&fim=2026-06-30
     * Retorna as vendas dentro de um intervalo de datas (RF-06).
     */
    @GetMapping("/por-periodo")
    public ResponseEntity<Map<String, Object>> relatorioPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
    ) {
        List<Venda> vendas = vendaService.buscarPorPeriodo(
                inicio.atStartOfDay(),
                fim.atTime(23, 59, 59)
        );

        List<VendaResponseDTO> vendasDTO = vendas.stream()
                .map(v -> toResponseDTO(v))
                .collect(Collectors.toList());

        BigDecimal valorTotal = vendas.stream()
                .map(Venda::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Long> produtosMaisVendidos = vendas.stream()
                .flatMap(v -> v.getItens().stream())
                .collect(Collectors.groupingBy(
                        item -> item.getProduto().getNome(),
                        Collectors.summingLong(item -> item.getQuantidade().longValue())
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("periodo", inicio + " até " + fim);
        resposta.put("totalVendas", vendasDTO.size());
        resposta.put("valorTotalArrecadado", valorTotal);
        resposta.put("produtosMaisVendidos", produtosMaisVendidos);
        resposta.put("vendas", vendasDTO);

        return ResponseEntity.ok(resposta);
    }

    /**
     * GET /api/relatorios/por-cliente?clienteId=1
     * Retorna o histórico completo de compras de um cliente (RF-07).
     */
    @GetMapping("/por-cliente")
    public ResponseEntity<Map<String, Object>> relatorioPorCliente(
            @RequestParam Long clienteId
    ) {
        List<Venda> vendas = vendaService.buscarPorClienteId(clienteId);

        List<VendaResponseDTO> vendasDTO = vendas.stream()
                .map(v -> toResponseDTO(v))
                .collect(Collectors.toList());

        BigDecimal valorAcumulado = vendas.stream()
                .map(Venda::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String nomeCliente = vendas.isEmpty()
                ? "Cliente #" + clienteId
                : vendas.get(0).getCliente().getNome();

        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("clienteId", clienteId);
        resposta.put("nomeCliente", nomeCliente);
        resposta.put("totalCompras", vendasDTO.size());
        resposta.put("valorTotalAcumulado", valorAcumulado);
        resposta.put("historico", vendasDTO);

        return ResponseEntity.ok(resposta);
    }

    /**
     * GET /api/relatorios/grafico-anual?ano=2026
     * Retorna os dados mensais de volume de vendas para o gráfico (RF-08).
     */
    @GetMapping("/grafico-anual")
    public ResponseEntity<Map<String, Object>> graficoAnual(
            @RequestParam(defaultValue = "2026") int ano
    ) {
        List<Venda> todasVendas = vendaService.listarTodas();

        String[] meses = {"Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"};

        Map<String, Long> vendasPorMes = new LinkedHashMap<>();
        for (int i = 0; i < meses.length; i++) {
            final int mes = i + 1;
            long quantidade = todasVendas.stream()
                    .filter(v -> v.getDataVenda().getYear() == ano
                            && v.getDataVenda().getMonthValue() == mes)
                    .count();
            vendasPorMes.put(meses[i], quantidade);
        }

        long totalAnual = vendasPorMes.values().stream()
                .mapToLong(Long::longValue)
                .sum();

        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("ano", ano);
        resposta.put("vendasPorMes", vendasPorMes);
        resposta.put("totalAnual", totalAnual);

        return ResponseEntity.ok(resposta);
    }

    // ==========================================================================
    // Método de conversão entre Entidade e DTO
    // ==========================================================================

    private VendaResponseDTO toResponseDTO(Venda venda) {
        List<ItemVendaDTO> itensDTO = venda.getItens().stream()
                .map(item -> new ItemVendaDTO(
                        item.getId(),
                        item.getProduto().getId(),
                        item.getProduto().getNome(),
                        item.getQuantidade(),
                        item.getPrecoUnitario(),
                        item.getSubtotal()
                ))
                .collect(Collectors.toList());

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