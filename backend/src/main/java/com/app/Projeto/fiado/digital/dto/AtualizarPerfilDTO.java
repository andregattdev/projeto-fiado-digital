package com.app.Projeto.fiado.digital.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AtualizarPerfilDTO {

    @NotBlank(message = "Nome da loja é obrigatório")
    @Size(max = 120)
    private String nomeLoja;

    @NotBlank(message = "Nome do responsável é obrigatório")
    @Size(max = 120)
    private String nomeResponsavel;

    @Size(max = 20, message = "O telefone não pode exceder 20 caracteres")
    private String telefone;

    private String senhaAtual;

    @Size(min = 8, message = "A nova senha deve ter no mínimo 8 caracteres")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[!@#$%^&*(),.?\":{}|<>]).*$", message = "A nova senha deve conter pelo menos uma letra maiúscula e um caractere especial")
    private String novaSenha;

    private String cnpj;
}