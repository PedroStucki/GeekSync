package br.com.geeksync.domain.exception;

/**
 * Lançada quando se tenta vender um produto com estoque insuficiente (RF-03).
 * Mapeada pelo GlobalExceptionHandler para retornar HTTP 422 Unprocessable Entity.
 */
public class EstoqueInsuficienteException extends RuntimeException {

    public EstoqueInsuficienteException(String nomeProduto, int estoqueAtual, int quantidadeSolicitada) {
        super(String.format(
                "Estoque insuficiente para '%s'. Disponível: %d unidade(s). Solicitado: %d unidade(s).",
                nomeProduto, estoqueAtual, quantidadeSolicitada
        ));
    }
}