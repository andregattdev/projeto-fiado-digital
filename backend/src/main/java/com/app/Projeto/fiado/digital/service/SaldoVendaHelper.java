package com.app.Projeto.fiado.digital.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Component;

import com.app.Projeto.fiado.digital.model.Venda;
import com.app.Projeto.fiado.digital.repository.PagamentoRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SaldoVendaHelper {

    private final PagamentoRepository pagamentoRepository;

    public BigDecimal calcularSaldoDevedor(Venda venda) {
        BigDecimal pago = pagamentoRepository.somarPagamentosDaVenda(venda.getId());
        return venda.getValorTotal().subtract(pago);
    }

    public int calcularDiasEmAtraso(LocalDateTime dataVenda) {
        return (int) ChronoUnit.DAYS.between(dataVenda.toLocalDate(), LocalDateTime.now().toLocalDate());
    }
}
