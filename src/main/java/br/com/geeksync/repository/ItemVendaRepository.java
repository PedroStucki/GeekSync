package br.com.geeksync.repository;

import br.com.geeksync.domain.entity.ItemVenda;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemVendaRepository extends JpaRepository<ItemVenda, Long> {
}