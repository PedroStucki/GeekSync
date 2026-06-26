package br.com.geeksync.dto;

import br.com.geeksync.domain.enums.CategoriaProduto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * DTO para transferência de dados de Produto.
 * Usado em POST /api/produtos e PUT /api/produtos/{id}.
 * A anotação @Positive garante que o preço nunca seja zero ou negativo (RF-02).
 */
public record ProdutoDTO(

        Long id,

        @NotBlank(message = "O nome do produto é obrigatório.")
        String nome,

        String descricao,

        @NotNull(message = "O preço é obrigatório.")
        @Positive(message = "O preço deve ser maior que zero.")
        BigDecimal preco,

        @NotNull(message = "A quantidade em estoque é obrigatória.")
        @Min(value = 0, message = "A quantidade em estoque não pode ser negativa.")
        Integer qtdEstoque,

        @Min(value = 0, message = "O estoque mínimo não pode ser negativo.")
        Integer estoqueMinimo,

        @NotNull(message = "A categoria do produto é obrigatória.")
        CategoriaProduto categoria,

        // Campo calculado para o frontend exibir o alerta de estoque baixo (RF-09)
        Boolean estoqueAbaixoDoMinimo
) {}