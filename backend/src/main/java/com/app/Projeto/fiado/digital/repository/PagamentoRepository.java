package com.app.Projeto.fiado.digital.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.app.Projeto.fiado.digital.model.Pagamento;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    List<Pagamento> findByVendaId(Long vendaId);

    @Query("SELECT COALESCE(SUM(p.valor), 0) FROM Pagamento p WHERE p.venda.id = :vendaId")
    BigDecimal somarPagamentosDaVenda(Long vendaId);

    @Query("SELECT COALESCE(SUM(p.valor), 0) FROM Pagamento p " +
            "WHERE p.venda.cliente.comercio.id = :comercioId " +
            "AND p.dataPagamento BETWEEN :inicio AND :fim")
    BigDecimal calcularTotalRecebidoPeriodoByComercioId(
            @Param("comercioId") Long comercioId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim);
}
