package br.com.geeksync.strategy;

import br.com.geeksync.domain.entity.Venda;
import java.math.BigDecimal;

public class SemDescontoStrategy implements DescontoStrategy {

    @Override
    public BigDecimal calcular(Venda venda) {
        return BigDecimal.ZERO; // Implementação neutra
    }
}