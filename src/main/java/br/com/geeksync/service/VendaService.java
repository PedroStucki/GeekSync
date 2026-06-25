package br.com.geeksync.service;

import br.com.geeksync.domain.entity.*;
import br.com.geeksync.domain.enums.StatusVenda;
import br.com.geeksync.repository.*;
import br.com.geeksync.strategy.DescontoLivrosStrategy;
import br.com.geeksync.strategy.DescontoStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VendaService {

    private final VendaRepository vendaRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProdutoRepository produtoRepository;

    @Transactional
    public Venda realizarVenda(Long clienteId, Long usuarioId, Venda venda) {
        if (venda.getItens() == null || venda.getItens().isEmpty()) {
            throw new RuntimeException("Uma venda deve conter pelo menos 1 item.");
        }

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado."));
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário vendedor não encontrado."));

        venda.setCliente(cliente);
        venda.setUsuario(usuario);
        venda.setStatus(StatusVenda.ABERTA);
        venda.setDataVenda(LocalDateTime.now());

        BigDecimal subtotalBruto = BigDecimal.ZERO;

        for (ItemVenda item : venda.getItens()) {
            Produto produto = produtoRepository.findById(item.getProduto().getId())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado."));

            if (produto.getQtdEstoque() < item.getQuantidade()) {
                throw new RuntimeException("Estoque insuficiente para o produto: " + produto.getNome());
            }

            item.setPrecoUnitario(produto.getPreco());
            item.setSubtotal(produto.getPreco().multiply(new BigDecimal(item.getQuantidade())));
            item.setVenda(venda);
            item.setProduto(produto);

            subtotalBruto = subtotalBruto.add(item.getSubtotal());
        }

        DescontoStrategy strategy = new DescontoLivrosStrategy();
        BigDecimal valorDesconto = strategy.calcular(venda);

        venda.setDesconto(valorDesconto);
        venda.setValorTotal(subtotalBruto.subtract(valorDesconto));

        return vendaRepository.save(venda);
    }

    @Transactional
    public void cancelarVenda(Long vendaId) {
        Venda venda = vendaRepository.findById(vendaId)
                .orElseThrow(() -> new RuntimeException("Venda não encontrada."));

        if (venda.getStatus() != StatusVenda.ABERTA) {
            throw new RuntimeException("Apenas vendas com status ABERTA podem ser canceladas.");
        }

        venda.setStatus(StatusVenda.CANCELADA);

        for (ItemVenda item : venda.getItens()) {
            Produto produto = item.getProduto();
            produto.setQtdEstoque(produto.getQtdEstoque() + item.getQuantidade());
            produtoRepository.save(produto);
        }

        vendaRepository.save(venda);
    }

    // ============================================================
    // Métodos adicionados para suporte ao VendaController
    // ============================================================

    public List<Venda> listarTodas() {
        return vendaRepository.findAll();
    }

    public Venda buscarPorId(Long id) {
        return vendaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venda não encontrada com o ID: " + id));
    }

    // ============================================================
    // Métodos adicionados para suporte ao RelatorioController
    // ============================================================

    public List<Venda> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return vendaRepository.findByDataVendaBetween(inicio, fim);
    }

    public List<Venda> buscarPorClienteId(Long clienteId) {
        return vendaRepository.findByClienteId(clienteId);
    }
}