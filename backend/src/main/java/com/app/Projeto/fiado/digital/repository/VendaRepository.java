package com.app.Projeto.fiado.digital.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.app.Projeto.fiado.digital.dto.EvolucaoMensalProj;
import com.app.Projeto.fiado.digital.model.Venda;

public interface VendaRepository extends JpaRepository<Venda, Long> {

        List<Venda> findByClienteIdAndClienteComercioId(Long clienteId, Long comercioId);

        Optional<Venda> findByIdAndClienteComercioId(Long id, Long comercioId);

        @Query("SELECT v FROM Venda v " +
                        "WHERE v.cliente.comercio.id = :comercioId " +
                        "AND v.ativa = true " +
                        "AND v.valorTotal > (SELECT COALESCE(SUM(p.valor), 0) FROM Pagamento p WHERE p.venda = v)")
        List<Venda> findVendasEmAbertoByComercioId(@Param("comercioId") Long comercioId);

        @Query("SELECT v FROM Venda v " +
                        "WHERE v.cliente.id = :clienteId " +
                        "AND v.cliente.comercio.id = :comercioId " +
                        "AND v.ativa = true " +
                        "AND v.valorTotal > (SELECT COALESCE(SUM(p.valor), 0) FROM Pagamento p WHERE p.venda = v)")
        List<Venda> findVendasEmAbertoPorClienteAndComercioId(
                        @Param("clienteId") Long clienteId,
                        @Param("comercioId") Long comercioId);

        @Query("SELECT COALESCE(SUM(v.valorTotal), 0) FROM Venda v " +
                        "WHERE v.cliente.comercio.id = :comercioId " +
                        "AND v.dataVenda BETWEEN :inicio AND :fim " +
                        "AND v.ativa = true")
        BigDecimal calcularTotalVendasPeriodoByComercioId(
                        @Param("comercioId") Long comercioId,
                        @Param("inicio") LocalDateTime inicio,
                        @Param("fim") LocalDateTime fim);

        List<Venda> findTop10ByClienteComercioIdOrderByDataVendaDesc(Long comercioId);

        List<Venda> findByClienteComercioIdAndDataVendaBetweenOrderByDataVendaDesc(
                        Long comercioId, LocalDateTime inicio, LocalDateTime fim);

        @Query("SELECT v FROM Venda v " +
                        "WHERE v.cliente.comercio.id = :comercioId " +
                        "AND v.ativa = true " +
                        "AND v.dataVenda < :dataLimite " +
                        "AND v.valorTotal > (SELECT COALESCE(SUM(p.valor), 0) FROM Pagamento p WHERE p.venda = v) " +
                        "ORDER BY v.dataVenda ASC")
        List<Venda> findVendasVencidasByComercioId(
                        @Param("comercioId") Long comercioId,
                        @Param("dataLimite") LocalDateTime dataLimite);

        // QUERY OTIMIZADA: Agrupa as vendas e pagamentos por mês diretamente no banco de dados
        @Query(value = """
                        WITH vendas_mes AS (
                            SELECT 
                                TO_CHAR(v.data_venda, 'MM') as mesNumero,
                                SUM(v.valor_total) as vendido
                            FROM vendas v
                            INNER JOIN clientes c ON v.cliente_id = c.id
                            WHERE c.comercio_id = :comercioId
                              AND v.ativa = true
                              AND v.data_venda >= CURRENT_DATE - INTERVAL '6 months'
                            GROUP BY TO_CHAR(v.data_venda, 'MM')
                        ),
                        pagamentos_mes AS (
                            SELECT 
                                TO_CHAR(v.data_venda, 'MM') as mesNumero,
                                SUM(p.valor) as recebido
                            FROM pagamentos p
                            INNER JOIN vendas v ON p.venda_id = v.id
                            INNER JOIN clientes c ON v.cliente_id = c.id
                            WHERE c.comercio_id = :comercioId
                              AND v.ativa = true
                              AND v.data_venda >= CURRENT_DATE - INTERVAL '6 months'
                            GROUP BY TO_CHAR(v.data_venda, 'MM')
                        )
                        SELECT 
                            COALESCE(vm.mesNumero, pm.mesNumero) as mesNumero,
                            CASE COALESCE(vm.mesNumero, pm.mesNumero)
                                WHEN '01' THEN 'Jan' WHEN '02' THEN 'Fev' WHEN '03' THEN 'Mar'
                                WHEN '04' THEN 'Abr' WHEN '05' THEN 'Mai' WHEN '06' THEN 'Jun'
                                WHEN '07' THEN 'Jul' WHEN '08' THEN 'Ago' WHEN '09' THEN 'Set'
                                WHEN '10' THEN 'Out' WHEN '11' THEN 'Nov' WHEN '12' THEN 'Dez'
                            END as mesNome,
                            COALESCE(vm.vendido, 0) as vendido,
                            COALESCE(pm.recebido, 0) as recebido
                        FROM vendas_mes vm
                        FULL OUTER JOIN pagamentos_mes pm ON vm.mesNumero = pm.mesNumero
                        ORDER BY mesNumero ASC
                        """, nativeQuery = true)
        List<EvolucaoMensalProj> buscarEvolucaoFinanceira(@Param("comercioId") Long comercioId);
}