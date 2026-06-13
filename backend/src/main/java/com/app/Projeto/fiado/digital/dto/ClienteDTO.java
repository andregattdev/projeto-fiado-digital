package com.app.Projeto.fiado.digital.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDTO {

    private Long id;

    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
    private String nome;

    @NotBlank(message = "O CPF é obrigatório")
    @Pattern(regexp = "\\d{11}", message = "O CPF deve conter exatamente")
    private String cpf;
    
    @NotBlank(message = "O telefone é obrigatório")
    @Pattern(regexp = "\\d{11,15}", message = "Telefone deve conter apenas números e ter entre 11 e 15 dígitos")
    private String telefone;
    
    @NotBlank(message = "O endereço é obrigatório")
    @Size(min = 3, max = 200, message = "O endereço deve ter entre 3 e 200 caracteres")
    private String endereco;
    
    @NotNull(message = "A data de nascimento é obrigatória")
    @PastOrPresent(message = "A data de nascimento deve ser uma data passada ou presente")
    private LocalDate dataNascimento;
    
    private BigDecimal totalDevido;

}
