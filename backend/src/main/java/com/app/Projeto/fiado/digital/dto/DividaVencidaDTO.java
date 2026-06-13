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
public class DividaVencidaDTO {

    private Long vendaId;
    private Long clienteId;
    private String nomeCliente;
    private String telefone;
    private LocalDateTime dataVenda;
    private int diasEmAtraso;
    private BigDecimal saldoDevedor;
}
