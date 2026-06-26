package br.com.geeksync.service;

import br.com.geeksync.domain.entity.Cliente;
import br.com.geeksync.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    @Transactional
    public Cliente salvar(Cliente cliente) {
        // Validação de duplicidade exigida no RF-01
        if (cliente.getId() == null && clienteRepository.existsByCpf(cliente.getCpf())) {
            throw new RuntimeException("Já existe um cliente cadastrado com o CPF: " + cliente.getCpf());
        }
        if (cliente.getId() == null && clienteRepository.existsByEmail(cliente.getEmail())) {
            throw new RuntimeException("Já existe um cliente cadastrado com o E-mail: " + cliente.getEmail());
        }
        return clienteRepository.save(cliente);
    }

    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado com o ID: " + id));
    }

    @Transactional
    public void excluir(Long id) {
        Cliente cliente = buscarPorId(id);
        // Se ele tiver vendas, o próprio banco MySQL vai barrar a deleção por causa da constraint RESTRICT!
        clienteRepository.delete(cliente);
    }
}