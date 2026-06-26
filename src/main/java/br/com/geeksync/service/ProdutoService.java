package br.com.geeksync.service;

import br.com.geeksync.domain.entity.Produto;
import br.com.geeksync.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    @Transactional
    public Produto salvar(Produto produto) {
        if (produto.getPreco().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("O preço do produto não pode ser zero ou negativo.");
        }
        if (produto.getQtdEstoque() < 0) {
            throw new RuntimeException("A quantidade em estoque não pode ser negativa.");
        }
        return produtoRepository.save(produto);
    }

    public List<Produto> listarTodos() {
        return produtoRepository.findAll();
    }

    public Produto buscarPorId(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado com o ID: " + id));
    }

    // Gatilho para o Alerta de Estoque Baixo (RF-09)
    public boolean necessitaReposicao(Produto produto) {
        return produto.getQtdEstoque() <= produto.getEstoqueMinimo();
    }

    @Transactional
    public void excluir(Long id) {
        Produto produto = buscarPorId(id);
        produtoRepository.delete(produto);
    }
}