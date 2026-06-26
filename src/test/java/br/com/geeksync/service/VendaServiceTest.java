package br.com.geeksync.service;

import br.com.geeksync.domain.entity.*;
import br.com.geeksync.domain.enums.CategoriaProduto;
import br.com.geeksync.domain.enums.PerfilUsuario;
import br.com.geeksync.domain.enums.StatusVenda;
import br.com.geeksync.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários - VendaService")
class VendaServiceTest {

    @Mock
    private VendaRepository vendaRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private VendaService vendaService;

    private Cliente cliente;
    private Usuario usuario;
    private Produto produto;
    private Venda venda;
    private ItemVenda item;

    @BeforeEach
    void setUp() {
        cliente = Cliente.builder()
                .id(1L)
                .nome("João Silva")
                .cpf("12345678901")
                .email("joao@email.com")
                .build();

        usuario = Usuario.builder()
                .id(1L)
                .username("vendedor01")
                .senha("$2a$12$hash")
                .perfil(PerfilUsuario.VENDEDOR)
                .build();

        produto = Produto.builder()
                .id(1L)
                .nome("One Piece Vol. 105")
                .preco(new BigDecimal("34.90"))
                .qtdEstoque(10)
                .estoqueMinimo(5)
                .categoria(CategoriaProduto.MANGA)
                .build();

        item = ItemVenda.builder()
                .produto(Produto.builder().id(1L).build())
                .quantidade(2)
                .build();

        List<ItemVenda> itens = new ArrayList<>();
        itens.add(item);

        venda = Venda.builder()
                .id(1L)
                .status(StatusVenda.ABERTA)
                .dataVenda(LocalDateTime.now())
                .build();
        venda.setItens(itens);
    }

    // ============================================================
    // TESTES - realizarVenda()
    // ============================================================

    @Test
    @DisplayName("Deve realizar venda com sucesso")
    void deveRealizarVendaComSucesso() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(vendaRepository.save(any(Venda.class))).thenReturn(venda);

        Venda salva = vendaService.realizarVenda(1L, 1L, venda);

        assertNotNull(salva);
        verify(vendaRepository, times(1)).save(any(Venda.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando venda não tem itens")
    void deveLancarExcecaoQuandoVendaSemItens() {
        venda.setItens(new ArrayList<>());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> vendaService.realizarVenda(1L, 1L, venda));

        assertTrue(ex.getMessage().contains("item"));
        verify(vendaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando cliente não encontrado")
    void deveLancarExcecaoQuandoClienteNaoEncontrado() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> vendaService.realizarVenda(99L, 1L, venda));

        assertTrue(ex.getMessage().contains("Cliente"));
        verify(vendaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando estoque insuficiente")
    void deveLancarExcecaoQuandoEstoqueInsuficiente() {
        produto.setQtdEstoque(1);
        item.setQuantidade(5);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> vendaService.realizarVenda(1L, 1L, venda));

        assertTrue(ex.getMessage().contains("Estoque insuficiente") ||
                ex.getMessage().contains("estoque"));
        verify(vendaRepository, never()).save(any());
    }

    // ============================================================
    // TESTES - cancelarVenda()
    // ============================================================

    @Test
    @DisplayName("Deve cancelar venda com status ABERTA")
    void deveCancelarVendaComStatusAberta() {
        item.setProduto(produto);
        item.setQuantidade(2);
        venda.setStatus(StatusVenda.ABERTA);

        when(vendaRepository.findById(1L)).thenReturn(Optional.of(venda));
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);
        when(vendaRepository.save(any(Venda.class))).thenReturn(venda);

        assertDoesNotThrow(() -> vendaService.cancelarVenda(1L));
        assertEquals(StatusVenda.CANCELADA, venda.getStatus());
        verify(vendaRepository, times(1)).save(venda);
    }

    @Test
    @DisplayName("Deve lançar exceção ao cancelar venda já cancelada")
    void deveLancarExcecaoAoCancelarVendaJaCancelada() {
        venda.setStatus(StatusVenda.CANCELADA);
        when(vendaRepository.findById(1L)).thenReturn(Optional.of(venda));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> vendaService.cancelarVenda(1L));

        assertTrue(ex.getMessage().contains("ABERTA"));
        verify(vendaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao cancelar venda inexistente")
    void deveLancarExcecaoAoCancelarVendaInexistente() {
        when(vendaRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> vendaService.cancelarVenda(99L));

        assertTrue(ex.getMessage().contains("encontrada") ||
                ex.getMessage().contains("99"));
        verify(vendaRepository, never()).save(any());
    }

    // ============================================================
    // TESTES - listarTodas() e buscarPorId()
    // ============================================================

    @Test
    @DisplayName("Deve listar todas as vendas")
    void deveListarTodasAsVendas() {
        when(vendaRepository.findAll()).thenReturn(List.of(venda));

        List<Venda> vendas = vendaService.listarTodas();

        assertFalse(vendas.isEmpty());
        assertEquals(1, vendas.size());
    }

    @Test
    @DisplayName("Deve buscar venda por ID com sucesso")
    void deveBuscarVendaPorIdComSucesso() {
        when(vendaRepository.findById(1L)).thenReturn(Optional.of(venda));

        Venda encontrada = vendaService.buscarPorId(1L);

        assertNotNull(encontrada);
        assertEquals(1L, encontrada.getId());
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar venda com ID inexistente")
    void deveLancarExcecaoAoBuscarVendaComIdInexistente() {
        when(vendaRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> vendaService.buscarPorId(99L));

        assertTrue(ex.getMessage().contains("99"));
    }
}