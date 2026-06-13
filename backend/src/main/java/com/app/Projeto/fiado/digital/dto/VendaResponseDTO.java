package com.app.Projeto.fiado.digital.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VendaResponseDTO {

    private Long id;
    private Long clienteId;
    private String nomeCliente;
    private String cpfCliente;
    private LocalDateTime dataVenda;
    private BigDecimal valorTotal;
    private String observacao;
    private boolean ativa;
    private List<VendaItemDTO> itens;
    private BigDecimal valorPago;
    private BigDecimal saldoDevedor;
}