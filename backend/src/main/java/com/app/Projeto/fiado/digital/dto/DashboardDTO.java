package com.app.Projeto.fiado.digital.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {

    private BigDecimal totalAReceber;
    private BigDecimal totalRecebidoHoje;
    private Integer quantidadeVendasEmAberto;
    private Integer quantidadeClientesDevedores;
    
    private List<ClienteResumoDTO> maioresDevedores;
    private List<VendaResumoDTO> ultimasVendas;

    private Integer quantidadeDividasVencidas;
    private BigDecimal totalDividasVencidas;
    private List<DividaVencidaDTO> dividasVencidas;

    private List<EvolucaoMensalDTO> evolucaoMensal;
}
