package com.app.Projeto.fiado.digital.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.Projeto.fiado.digital.model.Comercio;

public interface ComercioRepository extends JpaRepository<Comercio, Long> {

    Optional<Comercio> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByCnpj(String cnpj);

    boolean existsByCnpjAndIdNot(String cnpj, Long id);
}
