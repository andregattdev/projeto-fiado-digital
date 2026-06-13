package com.app.Projeto.fiado.digital.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.app.Projeto.fiado.digital.dto.PagamentoDTO;
import com.app.Projeto.fiado.digital.exception.BusinessException;
import com.app.Projeto.fiado.digital.exception.ResourceNotFoundException;
import com.app.Projeto.fiado.digital.model.Pagamento;
import com.app.Projeto.fiado.digital.model.Venda;
import com.app.Projeto.fiado.digital.integration.whatsapp.WhatsAppNotificacaoService;
import com.app.Projeto.fiado.digital.repository.PagamentoRepository;
import com.app.Projeto.fiado.digital.repository.VendaRepository;
import com.app.Projeto.fiado.digital.security.ComercioContextService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final VendaRepository vendaRepository;
    private final SaldoVendaHelper saldoVendaHelper;
    private final WhatsAppNotificacaoService whatsAppNotificacaoService;
    private final ComercioContextService comercioContext;

    @Transactional
    public PagamentoDTO registrarPagamento(PagamentoDTO dto) {
        Long comercioId = comercioContext.getComercioIdLogado();
        Venda venda = vendaRepository.findByIdAndClienteComercioId(dto.getVendaId(), comercioId)
                .orElseThrow(() -> new ResourceNotFoundException("Venda não encontrada"));

        if (!venda.isAtiva()) {
            throw new BusinessException("Não é possível registrar pagamento em venda cancelada");
        }

        BigDecimal saldoDevedor = saldoVendaHelper.calcularSaldoDevedor(venda);
        if (saldoDevedor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Esta venda já está quitada");
        }

        if (dto.getValor().compareTo(saldoDevedor) > 0) {
            throw new BusinessException(
                    "Valor do pagamento (R$ " + dto.getValor() + ") excede o saldo devedor (R$ " + saldoDevedor + ")");
        }

        Pagamento pagamento = new Pagamento();
        pagamento.setVenda(venda);
        pagamento.setValor(dto.getValor());
        pagamento.setDataPagamento(LocalDateTime.now());
        pagamento.setFormaPagamento(dto.getFormaPagamento());
        pagamento.setObservacao(dto.getObservacao());

        pagamento = pagamentoRepository.save(pagamento);

        BigDecimal saldoApos = saldoVendaHelper.calcularSaldoDevedor(venda);
        if (saldoApos.compareTo(BigDecimal.ZERO) <= 0) {
            whatsAppNotificacaoService.notificarQuitacao(venda);
        } else {
            whatsAppNotificacaoService.notificarPagamentoParcial(venda);
        }

        return converterParaDTO(pagamento);
    }

    public List<PagamentoDTO> listarPagamentosDaVenda(Long vendaId) {
        Long comercioId = comercioContext.getComercioIdLogado();
        if (!vendaRepository.findByIdAndClienteComercioId(vendaId, comercioId).isPresent()) {
            throw new ResourceNotFoundException("Venda não encontrada");
        }
        return pagamentoRepository.findByVendaId(vendaId).stream()
                .map(this::converterParaDTO)
                .toList();
    }

    private PagamentoDTO converterParaDTO(Pagamento pagamento) {
        PagamentoDTO dto = new PagamentoDTO();
        dto.setId(pagamento.getId());
        dto.setVendaId(pagamento.getVenda().getId());
        dto.setValor(pagamento.getValor());
        dto.setDataPagamento(pagamento.getDataPagamento());
        dto.setFormaPagamento(pagamento.getFormaPagamento());
        dto.setObservacao(pagamento.getObservacao());
        return dto;
    }
}
