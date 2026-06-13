package com.app.Projeto.fiado.digital.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.app.Projeto.fiado.digital.dto.ClienteResumoDTO;
import com.app.Projeto.fiado.digital.dto.DashboardDTO;
import com.app.Projeto.fiado.digital.dto.DividaVencidaDTO;
import com.app.Projeto.fiado.digital.dto.VendaResumoDTO;
import com.app.Projeto.fiado.digital.dto.EvolucaoMensalDTO;
import com.app.Projeto.fiado.digital.model.Venda;
import com.app.Projeto.fiado.digital.repository.ClienteRepository;
import com.app.Projeto.fiado.digital.repository.PagamentoRepository;
import com.app.Projeto.fiado.digital.repository.VendaRepository;
import com.app.Projeto.fiado.digital.security.ComercioContextService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {

        private final VendaRepository vendaRepository;
        private final PagamentoRepository pagamentoRepository;
        private final ClienteRepository clienteRepository;
        private final RelatorioService relatorioService;
        private final SaldoVendaHelper saldoVendaHelper;
        private final ComercioContextService comercioContext;

        public DashboardDTO gerarDashboard() {
                Long comercioId = comercioContext.getComercioIdLogado();

                List<Venda> vendasEmAberto = vendaRepository.findVendasEmAbertoByComercioId(comercioId);
                BigDecimal totalAReceber = vendasEmAberto.stream()
                                .map(saldoVendaHelper::calcularSaldoDevedor)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                LocalDateTime inicioDoDia = LocalDateTime.now().toLocalDate().atStartOfDay();
                LocalDateTime fimDoDia = inicioDoDia.plusDays(1).minusNanos(1);
                BigDecimal totalRecebidoHoje = pagamentoRepository.calcularTotalRecebidoPeriodoByComercioId(
                                comercioId, inicioDoDia, fimDoDia);

                int quantidadeVendasEmAberto = vendasEmAberto.size();

                var todosDevedores = clienteRepository.findClientesComSaldoDevedorByComercioId(comercioId);
                int quantidadeClientesDevedores = todosDevedores.size();

                List<ClienteResumoDTO> maioresDevedores = todosDevedores.stream()
                                .limit(5)
                                .map(proj -> new ClienteResumoDTO(
                                                proj.getClienteId(),
                                                proj.getNome(),
                                                proj.getCpf(),
                                                BigDecimal.valueOf(proj.getTotalDevido()),
                                                proj.getTelefone()))
                                .collect(Collectors.toList());

                List<VendaResumoDTO> ultimasVendas = vendaRepository
                                .findTop10ByClienteComercioIdOrderByDataVendaDesc(comercioId).stream()
                                .map(v -> {
                                        BigDecimal saldo = saldoVendaHelper.calcularSaldoDevedor(v);
                                        return new VendaResumoDTO(
                                                        v.getId(),
                                                        v.getCliente().getNome(),
                                                        v.getDataVenda(),
                                                        v.getValorTotal(),
                                                        saldo,
                                                        v.isAtiva());
                                })
                                .collect(Collectors.toList());

                List<DividaVencidaDTO> dividasVencidas = relatorioService.listarDividasVencidas(null);
                BigDecimal totalDividasVencidas = dividasVencidas.stream()
                                .map(DividaVencidaDTO::getSaldoDevedor)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                List<DividaVencidaDTO> topDividasVencidas = dividasVencidas.stream().limit(5)
                                .collect(Collectors.toList());

                // 1. BUSCA E AGRUPA OS DADOS DO BANCO
        java.util.Map<String, EvolucaoMensalDTO> mapaMeses = vendaRepository.buscarEvolucaoFinanceira(comercioId)
                .stream()
                .collect(Collectors.toMap(
                        p -> p.getMesNome() != null ? p.getMesNome() : "S/M",
                        p -> new EvolucaoMensalDTO(
                                p.getMesNome(),
                                p.getVendido() != null ? p.getVendido() : BigDecimal.ZERO,
                                p.getRecebido() != null ? p.getRecebido() : BigDecimal.ZERO
                        ),
                        (existente, novo) -> new EvolucaoMensalDTO(
                                existente.getMes(),
                                existente.getTotalVendido().add(novo.getTotalVendido()),
                                existente.getTotalRecebido().add(novo.getTotalRecebido())
                        )
                ));

        // 2. ORDENAÇÃO CRONOLÓGICA MANUAL: Garante que os meses sigam a sequência correta no gráfico
        List<String> ordemCronologica = List.of("Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez");

        List<EvolucaoMensalDTO> evolucaoMensal = ordemCronologica.stream()
                .filter(mapaMeses::containsKey) // Só inclui os meses que possuem dados retornados pelo banco
                .map(mapaMeses::get)
                .collect(Collectors.toList());

                // 2. RETORNO DO DTO PRINCIPAL
                return new DashboardDTO(
                                totalAReceber,
                                totalRecebidoHoje,
                                quantidadeVendasEmAberto,
                                quantidadeClientesDevedores,
                                maioresDevedores,
                                ultimasVendas,
                                dividasVencidas.size(),
                                totalDividasVencidas,
                                topDividasVencidas,
                                evolucaoMensal);
        }
}