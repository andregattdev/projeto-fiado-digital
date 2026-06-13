package com.app.Projeto.fiado.digital.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor 
@AllArgsConstructor 
public class EvolucaoMensalDTO {
    private String mes;
    private BigDecimal totalVendido;
    private BigDecimal totalRecebido;
}