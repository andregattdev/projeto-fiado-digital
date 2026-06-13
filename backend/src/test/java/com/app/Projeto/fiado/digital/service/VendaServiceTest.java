package com.app.Projeto.fiado.digital.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.app.Projeto.fiado.digital.exception.BusinessException;
import com.app.Projeto.fiado.digital.repository.ClienteRepository;
import com.app.Projeto.fiado.digital.repository.PagamentoRepository;
import com.app.Projeto.fiado.digital.integration.whatsapp.WhatsAppNotificacaoService;
import com.app.Projeto.fiado.digital.repository.VendaRepository;
import com.app.Projeto.fiado.digital.security.ComercioContextService;

import org.junit.jupiter.api.BeforeEach;

@ExtendWith(MockitoExtension.class)
class VendaServiceTest {

    @Mock
    private VendaRepository vendaRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private PagamentoRepository pagamentoRepository;

    @Mock
    private WhatsAppNotificacaoService whatsAppNotificacaoService;

    @Mock
    private ComercioContextService comercioContext;

    @InjectMocks
    private VendaService vendaService;

    @BeforeEach
    void setUp() {
        lenient().when(comercioContext.getComercioIdLogado()).thenReturn(1L);
    }

    @Test
    void listarPorPeriodo_deveFalharQuandoFimAntesDoInicio() {
        assertThrows(BusinessException.class,
                () -> vendaService.listarPorPeriodo(LocalDate.of(2026, 6, 10), LocalDate.of(2026, 6, 1)));
    }

    @Test
    void listarPorPeriodo_deveRetornarListaVazia() {
        LocalDate inicio = LocalDate.of(2026, 6, 1);
        LocalDate fim = LocalDate.of(2026, 6, 30);

        when(vendaRepository.findByClienteComercioIdAndDataVendaBetweenOrderByDataVendaDesc(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.eq(inicio.atStartOfDay()),
                org.mockito.ArgumentMatchers.eq(fim.atTime(23, 59, 59, 999_999_999))))
                .thenReturn(Collections.emptyList());

        var resultado = vendaService.listarPorPeriodo(inicio, fim);

        assertEquals(0, resultado.getQuantidadeVendas());
    }
}
