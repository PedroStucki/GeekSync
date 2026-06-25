package br.com.geeksync.controller;

import br.com.geeksync.domain.exception.CpfDuplicadoException;
import br.com.geeksync.domain.exception.EstoqueInsuficienteException;
import br.com.geeksync.domain.exception.RecursoNaoEncontradoException;
import br.com.geeksync.dto.ErroDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Intercepta TODAS as exceções lançadas pelos Controllers e as transforma
 * em respostas JSON limpas e padronizadas (ErroDTO).
 * O frontend nunca recebe um stack trace Java bruto — apenas mensagens de erro claras.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * HTTP 404 — Recurso não encontrado (cliente, produto, venda por ID inexistente).
     */
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroDTO> handleRecursoNaoEncontrado(RecursoNaoEncontradoException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErroDTO(404, "RECURSO_NAO_ENCONTRADO", ex.getMessage()));
    }

    /**
     * HTTP 409 — Conflito de dados: CPF já cadastrado no sistema.
     */
    @ExceptionHandler(CpfDuplicadoException.class)
    public ResponseEntity<ErroDTO> handleCpfDuplicado(CpfDuplicadoException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErroDTO(409, "CPF_DUPLICADO", ex.getMessage()));
    }

    /**
     * HTTP 422 — Regra de negócio violada: estoque insuficiente para a venda.
     */
    @ExceptionHandler(EstoqueInsuficienteException.class)
    public ResponseEntity<ErroDTO> handleEstoqueInsuficiente(EstoqueInsuficienteException ex) {
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErroDTO(422, "ESTOQUE_INSUFICIENTE", ex.getMessage()));
    }

    /**
     * HTTP 400 — Validação de campos falhou (@NotBlank, @Email, @Positive, etc.).
     * Captura todos os erros de campo e retorna a lista completa ao frontend.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroDTO> handleValidacao(MethodArgumentNotValidException ex) {
        List<String> detalhes = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .toList();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErroDTO(400, "DADOS_INVALIDOS", "Verifique os campos enviados.", detalhes));
    }

    /**
     * HTTP 400 — Qualquer outra RuntimeException não mapeada explicitamente acima.
     * Serve como "rede de segurança" para não vazar stack traces.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErroDTO> handleGenerico(RuntimeException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErroDTO(400, "ERRO_OPERACAO", ex.getMessage()));
    }
}