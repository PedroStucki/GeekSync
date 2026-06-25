package br.com.geeksync.domain.exception;

/**
 * Lançada quando se tenta cadastrar um cliente com CPF já existente no sistema.
 * Mapeada pelo GlobalExceptionHandler para retornar HTTP 409 Conflict.
 */
public class CpfDuplicadoException extends RuntimeException {

    public CpfDuplicadoException(String cpf) {
        super("Já existe um cliente cadastrado com o CPF: " + cpf);
    }
}