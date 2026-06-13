package com.app.Projeto.fiado.digital.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.app.Projeto.fiado.digital.config.FiadoProperties;
import com.app.Projeto.fiado.digital.dto.ClienteDividaRelatorioDTO;
import com.app.Projeto.fiado.digital.dto.DividaVencidaDTO;
import com.app.Projeto.fiado.digital.dto.RelatorioMensalDTO;
import com.app.Projeto.fiado.digital.model.Venda;
import com.app.Projeto.fiado.digital.repository.PagamentoRepository;
import com.app.Projeto.fiado.digital.repository.VendaRepository;
import com.app.Projeto.fiado.digital.security.ComercioContextService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RelatorioService {

    private final VendaRepository vendaRepository;
    private final PagamentoRepository pagamentoRepository;
    private final SaldoVendaHelper saldoVendaHelper;
    private final FiadoProperties fiadoProperties;
    private final ComercioContextService comercioContext;

    public RelatorioMensalDTO gerarRelatorioMensal(int mes, int ano) {
        Long comercioId = comercioContext.getComercioIdLogado();
        YearMonth yearMonth = YearMonth.of(ano, mes);
        LocalDateTime inicio = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime fim = yearMonth.atEndOfMonth().atTime(23, 59, 59, 999_999_999);

        BigDecimal totalVendasNoMes = vendaRepository.calcularTotalVendasPeriodoByComercioId(comercioId, inicio, fim);
        BigDecimal totalRecebidoNoMes = pagamentoRepository.calcularTotalRecebidoPeriodoByComercioId(
                comercioId, inicio, fim);

        List<ClienteDividaRelatorioDTO> devedores = montarListaDevedoresComTempo(comercioId);

        BigDecimal saldoDevedorTotal = devedores.stream()
                .map(ClienteDividaRelatorioDTO::getTotalDevido)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        RelatorioMensalDTO relatorio = new RelatorioMensalDTO();
        relatorio.setMes(mes);
        relatorio.setAno(ano);
        relatorio.setTotalVendasNoMes(totalVendasNoMes);
        relatorio.setTotalRecebidoNoMes(totalRecebidoNoMes);
        relatorio.setSaldoDevedorTotal(saldoDevedorTotal);
        relatorio.setQuantidadeDevedores(devedores.size());
        relatorio.setDevedores(devedores);
        return relatorio;
    }

    public List<DividaVencidaDTO> listarDividasVencidas(Integer diasVencimento) {
        Long comercioId = comercioContext.getComercioIdLogado();
        int dias = diasVencimento != null ? diasVencimento : fiadoProperties.getDiasVencimento();
        LocalDateTime dataLimite = LocalDateTime.now().minusDays(dias);

        return vendaRepository.findVendasVencidasByComercioId(comercioId, dataLimite).stream()
                .map(this::converterParaDividaVencida)
                .sorted(Comparator.comparingInt(DividaVencidaDTO::getDiasEmAtraso).reversed())
                .collect(Collectors.toList());
    }

    private List<ClienteDividaRelatorioDTO> montarListaDevedoresComTempo(Long comercioId) {
        List<Venda> vendasEmAberto = vendaRepository.findVendasEmAbertoByComercioId(comercioId);

        Map<Long, List<Venda>> porCliente = vendasEmAberto.stream()
                .collect(Collectors.groupingBy(v -> v.getCliente().getId()));

        List<ClienteDividaRelatorioDTO> resultado = new ArrayList<>();

        for (Map.Entry<Long, List<Venda>> entry : porCliente.entrySet()) {
            List<Venda> vendas = entry.getValue();
            Venda maisAntiga = vendas.stream()
                    .min(Comparator.comparing(Venda::getDataVenda))
                    .orElseThrow();

            BigDecimal totalDevido = vendas.stream()
                    .map(saldoVendaHelper::calcularSaldoDevedor)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (totalDevido.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            ClienteDividaRelatorioDTO dto = new ClienteDividaRelatorioDTO();
            dto.setClienteId(maisAntiga.getCliente().getId());
            dto.setNome(maisAntiga.getCliente().getNome());
            dto.setCpf(maisAntiga.getCliente().getCpf());
            dto.setTelefone(maisAntiga.getCliente().getTelefone());
            dto.setTotalDevido(totalDevido);
            dto.setVendaMaisAntiga(maisAntiga.getDataVenda());
            dto.setDiasEmAtraso(saldoVendaHelper.calcularDiasEmAtraso(maisAntiga.getDataVenda()));
            resultado.add(dto);
        }

        resultado.sort(Comparator.comparingInt(ClienteDividaRelatorioDTO::getDiasEmAtraso).reversed());
        return resultado;
    }

    private DividaVencidaDTO converterParaDividaVencida(Venda venda) {
        BigDecimal saldo = saldoVendaHelper.calcularSaldoDevedor(venda);
        DividaVencidaDTO dto = new DividaVencidaDTO();
        dto.setVendaId(venda.getId());
        dto.setClienteId(venda.getCliente().getId());
        dto.setNomeCliente(venda.getCliente().getNome());
        dto.setTelefone(venda.getCliente().getTelefone());
        dto.setDataVenda(venda.getDataVenda());
        dto.setDiasEmAtraso(saldoVendaHelper.calcularDiasEmAtraso(venda.getDataVenda()));
        dto.setSaldoDevedor(saldo);
        return dto;
    }
}
