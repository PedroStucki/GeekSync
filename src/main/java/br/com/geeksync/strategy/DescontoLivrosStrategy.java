package br.com.geeksync.strategy;

import br.com.geeksync.domain.entity.ItemVenda;
import br.com.geeksync.domain.entity.Venda;
import br.com.geeksync.domain.enums.CategoriaProduto;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class DescontoLivrosStrategy implements DescontoStrategy {

    @Override
    public BigDecimal calcular(Venda venda) {
        if (venda == null || venda.getItens() == null) {
            return BigDecimal.ZERO;
        }

        // 1. O Filtro: Varre a lista de itens e separa APENAS os que são MANGA ou LIVRO_FICCAO
        BigDecimal somaItensElegiveis = venda.getItens().stream()
                .filter(item -> item.getProduto() != null &&
                        (item.getProduto().getCategoria() == CategoriaProduto.MANGA ||
                                item.getProduto().getCategoria() == CategoriaProduto.LIVRO_FICCAO))
                .map(ItemVenda::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. A Regra: Se a soma DESSES itens específicos ultrapassar R$ 150,00...
        if (somaItensElegiveis.compareTo(new BigDecimal("150.00")) > 0) {
            // Aplica 10% (0.10) de desconto estritamente sobre o valor deles
            return somaItensElegiveis.multiply(new BigDecimal("0.10"))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return BigDecimal.ZERO; // Se deu R$ 149,90, chora: desconto zero!
    }
}