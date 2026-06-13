package com.app.Projeto.fiado.digital.config;

import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.app.Projeto.fiado.digital.model.Cliente;
import com.app.Projeto.fiado.digital.model.Comercio;
import com.app.Projeto.fiado.digital.repository.ClienteRepository;
import com.app.Projeto.fiado.digital.repository.ComercioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Associa clientes antigos (sem comércio) ao primeiro comércio cadastrado.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DadosLegadosMigration implements ApplicationRunner {

    private final ClienteRepository clienteRepository;
    private final ComercioRepository comercioRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<Cliente> semComercio = clienteRepository.findByComercioIsNull();
        if (semComercio.isEmpty()) {
            return;
        }
        Comercio comercio = comercioRepository.findAll().stream().findFirst().orElse(null);
        if (comercio == null) {
            return;
        }
        for (Cliente cliente : semComercio) {
            cliente.setComercio(comercio);
        }
        clienteRepository.saveAll(semComercio);
        log.info("Migrados {} clientes para o comércio id={}", semComercio.size(), comercio.getId());
    }
}
