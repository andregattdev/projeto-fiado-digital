package com.app.Projeto.fiado.digital.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClienteResumoDTO {

    private Long clienteId;
    private String nome;
    private String cpf;
    private BigDecimal totalDevido;
    private String telefone;
}
