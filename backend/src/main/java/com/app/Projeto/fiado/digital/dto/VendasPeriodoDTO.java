package com.app.Projeto.fiado.digital.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VendasPeriodoDTO {

    private LocalDate dataInicio;
    private LocalDate dataFim;
    private int quantidadeVendas;
    private BigDecimal totalVendas;
    private BigDecimal totalRecebido;
    private List<VendaResponseDTO> vendas;
}
