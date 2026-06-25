package br.com.geeksync.domain.exception;

/**
 * Lançada quando um recurso não é encontrado pelo ID informado.
 * Mapeada pelo GlobalExceptionHandler para retornar HTTP 404 Not Found.
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String recurso, Long id) {
        super(recurso + " não encontrado(a) com o ID: " + id);
    }
}