package com.app.Projeto.fiado.digital.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDividaRelatorioDTO {

    private Long clienteId;
    private String nome;
    private String telefone;
    private String cpf;
    private BigDecimal totalDevido;
    private LocalDateTime vendaMaisAntiga;
    private int diasEmAtraso;
}
