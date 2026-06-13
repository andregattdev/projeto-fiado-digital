package com.app.Projeto.fiado.digital.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RelatorioMensalDTO {

    private int mes;
    private int ano;
    private BigDecimal totalVendasNoMes;
    private BigDecimal totalRecebidoNoMes;
    private BigDecimal saldoDevedorTotal;
    private int quantidadeDevedores;
    private List<ClienteDividaRelatorioDTO> devedores;
}
