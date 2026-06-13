package com.app.Projeto.fiado.digital.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VendaItemDTO {

    private Long id;

    @NotBlank(message = "O campo 'produto' é obrigatório.")
    private String produto;

    @NotNull(message = "O campo 'quantidade' é obrigatório.")
    private Integer quantidade;

    @NotNull(message = "O campo 'precoUnitario' é obrigatório.")
    private BigDecimal precoUnitario;
    
    private BigDecimal subtotal;
}