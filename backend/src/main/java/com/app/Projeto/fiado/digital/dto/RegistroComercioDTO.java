package com.app.Projeto.fiado.digital.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistroComercioDTO {

    @NotBlank(message = "Nome da loja é obrigatório")
    @Size(max = 120)
    private String nomeLoja;

    @NotBlank(message = "Nome do responsável é obrigatório")
    @Size(max = 120)
    private String nomeResponsavel;

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    private String email;

    // ALTERAÇÃO: Aumentado para mínimo de 8 caracteres e adicionada a validação de
    // senha forte
    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[!@#$%^&*(),.?\":{}|<>]).*$", message = "A senha deve conter pelo menos uma letra maiúscula e um caractere especial")
    private String senha;

    @Size(max = 20)
    private String telefone;

    private String cnpj;
}