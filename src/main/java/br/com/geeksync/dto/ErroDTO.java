package br.com.geeksync.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de resposta para erros da API.
 * Garante que o frontend sempre receba erros em formato JSON limpo e previsível,
 * nunca a stack trace bruta do Java.
 *
 * Exemplo de resposta de erro:
 * {
 *   "timestamp": "2026-06-24T22:35:00",
 *   "status": 400,
 *   "erro": "CPF_DUPLICADO",
 *   "mensagem": "Já existe um cliente cadastrado com o CPF: 12345678901",
 *   "detalhes": []
 * }
 */
public record ErroDTO(
        LocalDateTime timestamp,
        int status,
        String erro,
        String mensagem,
        List<String> detalhes
) {
    // Construtor simplificado para erros sem detalhes adicionais
    public ErroDTO(int status, String erro, String mensagem) {
        this(LocalDateTime.now(), status, erro, mensagem, List.of());
    }

    // Construtor para erros de validação com lista de campos inválidos
    public ErroDTO(int status, String erro, String mensagem, List<String> detalhes) {
        this(LocalDateTime.now(), status, erro, mensagem, detalhes);
    }
}