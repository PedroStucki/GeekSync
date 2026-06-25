package br.com.geeksync.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para transferência de dados de Cliente.
 * Usado em POST /api/clientes e PUT /api/clientes/{id}.
 * As anotações de Bean Validation garantem que dados inválidos
 * sejam rejeitados antes mesmo de chegar ao Service.
 */
public record ClienteDTO(

        Long id,

        @NotBlank(message = "O nome do cliente é obrigatório.")
        @Size(max = 150, message = "O nome não pode ultrapassar 150 caracteres.")
        String nome,

        @NotBlank(message = "O CPF é obrigatório.")
        @Pattern(regexp = "\\d{11}", message = "O CPF deve conter exatamente 11 dígitos numéricos.")
        String cpf,

        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "Formato de e-mail inválido.")
        String email,

        String telefone,

        String endereco
) {}