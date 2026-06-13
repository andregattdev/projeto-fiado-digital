package com.app.Projeto.fiado.digital.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.app.Projeto.fiado.digital.dto.PagamentoDTO;
import com.app.Projeto.fiado.digital.exception.BusinessException;
import com.app.Projeto.fiado.digital.exception.ResourceNotFoundException;
import com.app.Projeto.fiado.digital.model.Pagamento;
import com.app.Projeto.fiado.digital.model.Venda;
import com.app.Projeto.fiado.digital.repository.PagamentoRepository;
import com.app.Projeto.fiado.digital.repository.VendaRepository;

@ExtendWith(MockitoExtension.class)
class PagamentoServiceTest {

    @Mock
    private VendaRepository vendaRepository;

    @Mock
    private PagamentoRepository pagamentoRepository;

    @Mock
    private SaldoVendaHelper saldoVendaHelper;

    @Mock
    private com.app.Projeto.fiado.digital.integration.whatsapp.WhatsAppNotificacaoService whatsAppNotificacaoService;

    @InjectMocks
    private PagamentoService pagamentoService;

    @Test
    void registrarPagamento_deveSalvarQuandoValorValido() {
        Venda venda = new Venda();
        venda.setId(1L);
        venda.setAtiva(true);
        venda.setValorTotal(new BigDecimal("100.00"));

        PagamentoDTO dto = new PagamentoDTO();
        dto.setVendaId(1L);
        dto.setValor(new BigDecimal("40.00"));
        dto.setFormaPagamento("PIX");

        when(vendaRepository.findById(1L)).thenReturn(Optional.of(venda));
        when(saldoVendaHelper.calcularSaldoDevedor(venda)).thenReturn(new BigDecimal("100.00"));
        when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(inv -> {
            Pagamento p = inv.getArgument(0);
            p.setId(10L);
            return p;
        });

        PagamentoDTO resultado = pagamentoService.registrarPagamento(dto);

        assertEquals(new BigDecimal("40.00"), resultado.getValor());
        verify(pagamentoRepository).save(any(Pagamento.class));
    }

    @Test
    void registrarPagamento_deveFalharQuandoVendaCancelada() {
        Venda venda = new Venda();
        venda.setId(1L);
        venda.setAtiva(false);

        PagamentoDTO dto = new PagamentoDTO();
        dto.setVendaId(1L);
        dto.setValor(new BigDecimal("10.00"));
        dto.setFormaPagamento("DINHEIRO");

        when(vendaRepository.findById(1L)).thenReturn(Optional.of(venda));

        assertThrows(BusinessException.class, () -> pagamentoService.registrarPagamento(dto));
    }

    @Test
    void registrarPagamento_deveFalharQuandoValorExcedeSaldo() {
        Venda venda = new Venda();
        venda.setId(1L);
        venda.setAtiva(true);

        PagamentoDTO dto = new PagamentoDTO();
        dto.setVendaId(1L);
        dto.setValor(new BigDecimal("150.00"));
        dto.setFormaPagamento("PIX");

        when(vendaRepository.findById(1L)).thenReturn(Optional.of(venda));
        when(saldoVendaHelper.calcularSaldoDevedor(venda)).thenReturn(new BigDecimal("50.00"));

        assertThrows(BusinessException.class, () -> pagamentoService.registrarPagamento(dto));
    }

    @Test
    void registrarPagamento_deveFalharQuandoVendaNaoExiste() {
        PagamentoDTO dto = new PagamentoDTO();
        dto.setVendaId(99L);
        dto.setValor(new BigDecimal("10.00"));
        dto.setFormaPagamento("PIX");

        when(vendaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> pagamentoService.registrarPagamento(dto));
    }
}
