package com.app.Projeto.fiado.digital.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.app.Projeto.fiado.digital.model.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    List<Cliente> findByComercioId(Long comercioId);

    List<Cliente> findByComercioIsNull();

    List<Cliente> findByComercioIdAndNomeContainingIgnoreCase(Long comercioId, String nome);

    Optional<Cliente> findByIdAndComercioId(Long id, Long comercioId);

    Optional<Cliente> findByCpf(String cpf);

    // Verifica se já existe esse CPF cadastrado neste comércio específico
    boolean existsByCpfAndComercioId(String cpf, Long comercioId);

    // Método usado no fluxo de atualizar
    Optional<Cliente> findByCpfAndComercioId(String cpf, Long comercioId);

    @Query("SELECT c.id as clienteId, " +
           "c.nome as nome, " +
           "c.cpf as cpf, " +
           "c.telefone as telefone, " +
           "COALESCE(SUM(v.valorTotal - (SELECT COALESCE(SUM(p.valor), 0) FROM Pagamento p WHERE p.venda = v)), 0) as totalDevido " +
           "FROM Cliente c " +
           "LEFT JOIN c.vendas v " +
           "WHERE c.comercio.id = :comercioId " +
           "AND v.ativa = true " +
           "GROUP BY c.id, c.nome, c.cpf, c.telefone " +
           "HAVING COALESCE(SUM(v.valorTotal - (SELECT COALESCE(SUM(p.valor), 0) FROM Pagamento p WHERE p.venda = v)), 0) > 0")
    List<ClienteDividaProjection> findClientesComSaldoDevedorByComercioId(@Param("comercioId") Long comercioId);
}
