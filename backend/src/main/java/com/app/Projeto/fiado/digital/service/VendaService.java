package com.app.Projeto.fiado.digital.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.app.Projeto.fiado.digital.dto.VendaItemDTO;
import com.app.Projeto.fiado.digital.dto.VendaRequestDTO;
import com.app.Projeto.fiado.digital.dto.VendaResponseDTO;
import com.app.Projeto.fiado.digital.dto.VendasPeriodoDTO;
import com.app.Projeto.fiado.digital.exception.BusinessException;
import com.app.Projeto.fiado.digital.exception.ResourceNotFoundException;
import com.app.Projeto.fiado.digital.model.Cliente;
import com.app.Projeto.fiado.digital.model.Venda;
import com.app.Projeto.fiado.digital.model.VendaItem;
import com.app.Projeto.fiado.digital.repository.ClienteRepository;
import com.app.Projeto.fiado.digital.repository.PagamentoRepository;
import com.app.Projeto.fiado.digital.integration.whatsapp.WhatsAppNotificacaoService;
import com.app.Projeto.fiado.digital.repository.VendaRepository;
import com.app.Projeto.fiado.digital.security.ComercioContextService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VendaService {

    private final VendaRepository vendaRepository;
    private final ClienteRepository clienteRepository;
    private final PagamentoRepository pagamentoRepository;
    private final WhatsAppNotificacaoService whatsAppNotificacaoService;
    private final ComercioContextService comercioContext;

    @Transactional
    public VendaResponseDTO criarVenda(VendaRequestDTO request) {
        Long comercioId = comercioContext.getComercioIdLogado();
        Cliente cliente = clienteRepository.findByIdAndComercioId(request.getClienteId(), comercioId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));

        Venda venda = new Venda();
        venda.setCliente(cliente);
        venda.setDataVenda(LocalDateTime.now());
        venda.setObservacao(request.getObservacao());

        // Adiciona os itens
        for (VendaItemDTO itemDTO : request.getItens()) {
            VendaItem item = new VendaItem();
            item.setVenda(venda);
            item.setProduto(itemDTO.getProduto());
            item.setQuantidade(itemDTO.getQuantidade());
            item.setPrecoUnitario(itemDTO.getPrecoUnitario());
            item.setSubtotal(itemDTO.getPrecoUnitario().multiply(BigDecimal.valueOf(itemDTO.getQuantidade())));

            venda.getItens().add(item);
        }

        // Calcula valor total
        BigDecimal total = venda.getItens().stream()
                .map(VendaItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        venda.setValorTotal(total);

        venda = vendaRepository.save(venda);
        whatsAppNotificacaoService.notificarNovaVendaFiado(venda);

        return converterParaResponseDTO(venda);
    }

    public List<VendaResponseDTO> listarVendasPorCliente(Long clienteId) {
        Long comercioId = comercioContext.getComercioIdLogado();
        return vendaRepository.findByClienteIdAndClienteComercioId(clienteId, comercioId).stream()
                .map(this::converterParaResponseDTO)
                .collect(Collectors.toList());
    }

    public List<VendaResponseDTO> listarVendasEmAberto() {
        Long comercioId = comercioContext.getComercioIdLogado();
        return vendaRepository.findVendasEmAbertoByComercioId(comercioId).stream()
                .map(this::converterParaResponseDTO)
                .collect(Collectors.toList());
    }

    public VendasPeriodoDTO listarPorPeriodo(LocalDate inicio, LocalDate fim) {
        if (fim.isBefore(inicio)) {
            throw new BusinessException("A data final deve ser igual ou posterior à data inicial");
        }

        LocalDateTime inicioDt = inicio.atStartOfDay();
        LocalDateTime fimDt = fim.atTime(23, 59, 59, 999_999_999);

        Long comercioId = comercioContext.getComercioIdLogado();
        List<VendaResponseDTO> vendas = vendaRepository
                .findByClienteComercioIdAndDataVendaBetweenOrderByDataVendaDesc(comercioId, inicioDt, fimDt).stream()
                .map(this::converterParaResponseDTO)
                .collect(Collectors.toList());

        BigDecimal totalVendas = vendas.stream()
                .filter(VendaResponseDTO::isAtiva)
                .map(VendaResponseDTO::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRecebido = vendas.stream()
                .map(v -> pagamentoRepository.somarPagamentosDaVenda(v.getId()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        VendasPeriodoDTO dto = new VendasPeriodoDTO();
        dto.setDataInicio(inicio);
        dto.setDataFim(fim);
        dto.setQuantidadeVendas(vendas.size());
        dto.setTotalVendas(totalVendas);
        dto.setTotalRecebido(totalRecebido);
        dto.setVendas(vendas);
        return dto;
    }

    @Transactional
    public void cancelarVenda(Long vendaId) {
        Long comercioId = comercioContext.getComercioIdLogado();
        Venda venda = vendaRepository.findByIdAndClienteComercioId(vendaId, comercioId)
                .orElseThrow(() -> new ResourceNotFoundException("Venda não encontrada"));

        if (!venda.isAtiva()) {
            throw new BusinessException("Venda já está cancelada");
        }

        venda.setAtiva(false);
        vendaRepository.save(venda);
    }

    private VendaResponseDTO converterParaResponseDTO(Venda venda) {
        BigDecimal valorPago = pagamentoRepository.somarPagamentosDaVenda(venda.getId());
        BigDecimal saldoDevedor = venda.getValorTotal().subtract(valorPago);

        VendaResponseDTO dto = new VendaResponseDTO();
        dto.setId(venda.getId());
        dto.setClienteId(venda.getCliente().getId());
        dto.setNomeCliente(venda.getCliente().getNome());
        dto.setCpfCliente(venda.getCliente().getCpf());
        dto.setDataVenda(venda.getDataVenda());
        dto.setValorTotal(venda.getValorTotal());
        dto.setObservacao(venda.getObservacao());
        dto.setAtiva(venda.isAtiva());
        dto.setValorPago(valorPago);
        dto.setSaldoDevedor(saldoDevedor);

        // Converte itens
        List<VendaItemDTO> itensDTO = venda.getItens().stream()
                .map(item -> new VendaItemDTO(item.getId(), item.getProduto(),
                        item.getQuantidade(), item.getPrecoUnitario(), item.getSubtotal()))
                .collect(Collectors.toList());

        dto.setItens(itensDTO);

        return dto;
    }

}
