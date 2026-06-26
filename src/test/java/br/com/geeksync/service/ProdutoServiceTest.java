package br.com.geeksync.service;

import br.com.geeksync.domain.entity.Produto;
import br.com.geeksync.domain.enums.CategoriaProduto;
import br.com.geeksync.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários - ProdutoService")
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private ProdutoService produtoService;

    private Produto produto;

    @BeforeEach
    void setUp() {
        produto = Produto.builder()
                .id(1L)
                .nome("One Piece Vol. 105")
                .descricao("Mangá Eiichiro Oda")
                .preco(new BigDecimal("34.90"))
                .qtdEstoque(10)
                .estoqueMinimo(5)
                .categoria(CategoriaProduto.MANGA)
                .build();
    }

    // ============================================================
    // TESTES - salvar()
    // ============================================================

    @Test
    @DisplayName("Deve salvar produto com sucesso quando preço é positivo")
    void deveSalvarProdutoComSucesso() {
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);

        Produto salvo = produtoService.salvar(produto);

        assertNotNull(salvo);
        assertEquals("One Piece Vol. 105", salvo.getNome());
        assertEquals(new BigDecimal("34.90"), salvo.getPreco());
        verify(produtoRepository, times(1)).save(produto);
    }

    @Test
    @DisplayName("Deve lançar exceção quando preço é zero")
    void deveLancarExcecaoQuandoPrecoEhZero() {
        produto.setPreco(BigDecimal.ZERO);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> produtoService.salvar(produto));

        assertTrue(ex.getMessage().contains("preço"));
        verify(produtoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando preço é negativo")
    void deveLancarExcecaoQuandoPrecoEhNegativo() {
        produto.setPreco(new BigDecimal("-10.00"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> produtoService.salvar(produto));

        assertTrue(ex.getMessage().contains("preço"));
        verify(produtoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando estoque é negativo")
    void deveLancarExcecaoQuandoEstoqueEhNegativo() {
        produto.setQtdEstoque(-1);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> produtoService.salvar(produto));

        assertTrue(ex.getMessage().contains("estoque"));
        verify(produtoRepository, never()).save(any());
    }

    // ============================================================
    // TESTES - listarTodos()
    // ============================================================

    @Test
    @DisplayName("Deve retornar lista de produtos")
    void deveRetornarListaDeProdutos() {
        when(produtoRepository.findAll()).thenReturn(List.of(produto));

        List<Produto> produtos = produtoService.listarTodos();

        assertFalse(produtos.isEmpty());
        assertEquals(1, produtos.size());
        assertEquals("One Piece Vol. 105", produtos.get(0).getNome());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há produtos")
    void deveRetornarListaVaziaQuandoNaoHaProdutos() {
        when(produtoRepository.findAll()).thenReturn(List.of());

        List<Produto> produtos = produtoService.listarTodos();

        assertTrue(produtos.isEmpty());
    }

    // ============================================================
    // TESTES - buscarPorId()
    // ============================================================

    @Test
    @DisplayName("Deve retornar produto quando ID existe")
    void deveRetornarProdutoQuandoIdExiste() {
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));

        Produto encontrado = produtoService.buscarPorId(1L);

        assertNotNull(encontrado);
        assertEquals(1L, encontrado.getId());
        assertEquals("One Piece Vol. 105", encontrado.getNome());
    }

    @Test
    @DisplayName("Deve lançar exceção quando ID não existe")
    void deveLancarExcecaoQuandoIdNaoExiste() {
        when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> produtoService.buscarPorId(99L));

        assertTrue(ex.getMessage().contains("99"));
    }

    // ============================================================
    // TESTES - necessitaReposicao()
    // ============================================================

    @Test
    @DisplayName("Deve retornar true quando estoque está abaixo do mínimo")
    void deveRetornarTrueQuandoEstoqueAbaixoDoMinimo() {
        produto.setQtdEstoque(3);
        produto.setEstoqueMinimo(5);

        assertTrue(produtoService.necessitaReposicao(produto));
    }

    @Test
    @DisplayName("Deve retornar true quando estoque igual ao mínimo")
    void deveRetornarTrueQuandoEstoqueIgualAoMinimo() {
        produto.setQtdEstoque(5);
        produto.setEstoqueMinimo(5);

        assertTrue(produtoService.necessitaReposicao(produto));
    }

    @Test
    @DisplayName("Deve retornar false quando estoque está acima do mínimo")
    void deveRetornarFalseQuandoEstoqueAcimaDoMinimo() {
        produto.setQtdEstoque(10);
        produto.setEstoqueMinimo(5);

        assertFalse(produtoService.necessitaReposicao(produto));
    }

    // ============================================================
    // TESTES - excluir()
    // ============================================================

    @Test
    @DisplayName("Deve excluir produto quando ID existe")
    void deveExcluirProdutoQuandoIdExiste() {
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        doNothing().when(produtoRepository).delete(produto);

        assertDoesNotThrow(() -> produtoService.excluir(1L));
        verify(produtoRepository, times(1)).delete(produto);
    }

    @Test
    @DisplayName("Deve lançar exceção ao excluir produto com ID inexistente")
    void deveLancarExcecaoAoExcluirProdutoInexistente() {
        when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> produtoService.excluir(99L));
        verify(produtoRepository, never()).delete(any());
    }
}