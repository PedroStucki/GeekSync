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
            throw new RuntimeException("Uma venda deve conter pelo menos 1 item."); // RF-03
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

        // 1. Valida estoques e calcula subtotais
        for (ItemVenda item : venda.getItens()) {
            Produto produto = produtoRepository.findById(item.getProduto().getId())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado."));

            if (produto.getQtdEstoque() < item.getQuantidade()) {
                throw new RuntimeException("Estoque insuficiente para o produto: " + produto.getNome()); // RF-03
            }

            item.setPrecoUnitario(produto.getPreco());
            item.setSubtotal(produto.getPreco().multiply(new BigDecimal(item.getQuantidade())));
            item.setVenda(venda);
            item.setProduto(produto);

            subtotalBruto = subtotalBruto.add(item.getSubtotal());
        }

        // 2. Chama o Strategy Pattern que você criou no passo anterior!
        DescontoStrategy strategy = new DescontoLivrosStrategy();
        BigDecimal valorDesconto = strategy.calcular(venda);

        venda.setDesconto(valorDesconto);
        venda.setValorTotal(subtotalBruto.subtract(valorDesconto));

        // Ao dar o save, o JPA insere os itens e dispara a Trigger do MySQL para abater o estoque
        return vendaRepository.save(venda);
    }

    // Implementação do Cancelamento (RF-10)
    @Transactional
    public void cancelarVenda(Long vendaId) {
        Venda venda = vendaRepository.findById(vendaId)
                .orElseThrow(() -> new RuntimeException("Venda não encontrada."));

        if (venda.getStatus() != StatusVenda.ABERTA) {
            throw new RuntimeException("Apenas vendas com status ABERTA podem ser canceladas."); // RF-10
        }

        venda.setStatus(StatusVenda.CANCELADA);

        // Como não há trigger de estorno no BD, o Java devolve o estoque manualmente:
        for (ItemVenda item : venda.getItens()) {
            Produto produto = item.getProduto();
            produto.setQtdEstoque(produto.getQtdEstoque() + item.getQuantidade());
            produtoRepository.save(produto);
        }

        vendaRepository.save(venda);
    }
}