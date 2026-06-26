package br.com.geeksync.strategy;

import br.com.geeksync.domain.entity.Venda;
import java.math.BigDecimal;

@FunctionalInterface // Exatamente como planejado na pág. 6 da sua documentação!
public interface DescontoStrategy {
    BigDecimal calcular(Venda venda);
}