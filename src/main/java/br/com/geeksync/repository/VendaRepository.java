package br.com.geeksync.repository;

import br.com.geeksync.domain.entity.Venda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface VendaRepository extends JpaRepository<Venda, Long> {

    // Para o Relatório por Período (RF-06) [cite: 19]
    List<Venda> findByDataVendaBetween(LocalDateTime inicio, LocalDateTime fim);

    // Para o Histórico do Cliente (RF-07) [cite: 19]
    List<Venda> findByClienteId(Long clienteId);
}