package com.app.Projeto.fiado.digital.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PagamentoDTO {

    private Long id;

    @NotNull(message = "O campo 'vendaId' é obrigatório.")
    private Long vendaId;

    @NotNull(message = "O campo 'valor' é obrigatório.")
    @Positive(message = "O valor do pagamento deve ser maior que zero.")
    private BigDecimal valor;

    private LocalDateTime dataPagamento;

    @NotNull(message = "O campo 'formaPagamento' é obrigatório.")
    private String formaPagamento;

    private String observacao;
}