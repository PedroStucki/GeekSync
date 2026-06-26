package br.com.geeksync.service;

import br.com.geeksync.domain.entity.Cliente;
import br.com.geeksync.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários - ClienteService")
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = Cliente.builder()
                .id(1L)
                .nome("João Silva")
                .cpf("12345678901")
                .email("joao@email.com")
                .telefone("(61) 99999-0001")
                .endereco("Asa Norte, Brasília")
                .build();
    }

    // ============================================================
    // TESTES - salvar()
    // ============================================================

    @Test
    @DisplayName("Deve salvar cliente com sucesso quando dados são válidos")
    void deveSalvarClienteComSucesso() {
        Cliente novoCliente = Cliente.builder()
                .nome("João Silva")
                .cpf("12345678901")
                .email("joao@email.com")
                .build();

        when(clienteRepository.existsByCpf("12345678901")).thenReturn(false);
        when(clienteRepository.existsByEmail("joao@email.com")).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        Cliente salvo = clienteService.salvar(novoCliente);

        assertNotNull(salvo);
        assertEquals("João Silva", salvo.getNome());
        verify(clienteRepository, times(1)).save(novoCliente);
    }

    @Test
    @DisplayName("Deve lançar exceção quando CPF já está cadastrado")
    void deveLancarExcecaoQuandoCpfDuplicado() {
        Cliente novoCliente = Cliente.builder()
                .nome("Maria")
                .cpf("12345678901")
                .email("maria@email.com")
                .build();

        when(clienteRepository.existsByCpf("12345678901")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> clienteService.salvar(novoCliente));

        assertTrue(ex.getMessage().contains("CPF"));
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando e-mail já está cadastrado")
    void deveLancarExcecaoQuandoEmailDuplicado() {
        Cliente novoCliente = Cliente.builder()
                .nome("Carlos")
                .cpf("98765432100")
                .email("joao@email.com")
                .build();

        when(clienteRepository.existsByCpf("98765432100")).thenReturn(false);
        when(clienteRepository.existsByEmail("joao@email.com")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> clienteService.salvar(novoCliente));

        assertTrue(ex.getMessage().contains("E-mail"));
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve atualizar cliente existente sem verificar CPF duplicado")
    void deveAtualizarClienteExistente() {
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        Cliente atualizado = clienteService.salvar(cliente);

        assertNotNull(atualizado);
        verify(clienteRepository, never()).existsByCpf(any());
        verify(clienteRepository, times(1)).save(cliente);
    }

    // ============================================================
    // TESTES - listarTodos()
    // ============================================================

    @Test
    @DisplayName("Deve retornar lista de clientes")
    void deveRetornarListaDeClientes() {
        when(clienteRepository.findAll()).thenReturn(List.of(cliente));

        List<Cliente> clientes = clienteService.listarTodos();

        assertFalse(clientes.isEmpty());
        assertEquals(1, clientes.size());
        assertEquals("João Silva", clientes.get(0).getNome());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há clientes")
    void deveRetornarListaVaziaQuandoNaoHaClientes() {
        when(clienteRepository.findAll()).thenReturn(List.of());

        List<Cliente> clientes = clienteService.listarTodos();

        assertTrue(clientes.isEmpty());
    }

    // ============================================================
    // TESTES - buscarPorId()
    // ============================================================

    @Test
    @DisplayName("Deve retornar cliente quando ID existe")
    void deveRetornarClienteQuandoIdExiste() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        Cliente encontrado = clienteService.buscarPorId(1L);

        assertNotNull(encontrado);
        assertEquals(1L, encontrado.getId());
        assertEquals("João Silva", encontrado.getNome());
    }

    @Test
    @DisplayName("Deve lançar exceção quando ID não existe")
    void deveLancarExcecaoQuandoIdNaoExiste() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> clienteService.buscarPorId(99L));

        assertTrue(ex.getMessage().contains("99"));
    }

    // ============================================================
    // TESTES - excluir()
    // ============================================================

    @Test
    @DisplayName("Deve excluir cliente quando ID existe")
    void deveExcluirClienteQuandoIdExiste() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        doNothing().when(clienteRepository).delete(cliente);

        assertDoesNotThrow(() -> clienteService.excluir(1L));
        verify(clienteRepository, times(1)).delete(cliente);
    }

    @Test
    @DisplayName("Deve lançar exceção ao excluir cliente com ID inexistente")
    void deveLancarExcecaoAoExcluirClienteInexistente() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> clienteService.excluir(99L));
        verify(clienteRepository, never()).delete(any());
    }
}