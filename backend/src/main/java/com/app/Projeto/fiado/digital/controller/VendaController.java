package com.app.Projeto.fiado.digital.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.Projeto.fiado.digital.dto.VendaRequestDTO;
import com.app.Projeto.fiado.digital.dto.VendaResponseDTO;
import com.app.Projeto.fiado.digital.dto.VendasPeriodoDTO;
import com.app.Projeto.fiado.digital.service.VendaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/vendas")
@RequiredArgsConstructor
public class VendaController {

    private final VendaService vendaService;

    @PostMapping
    public ResponseEntity<VendaResponseDTO> criarVenda(@Valid  @RequestBody VendaRequestDTO request) {
        VendaResponseDTO venda = vendaService.criarVenda(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(venda);
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<VendaResponseDTO>> listarPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(vendaService.listarVendasPorCliente(clienteId));
    }

    @GetMapping("/em-aberto")
    public ResponseEntity<List<VendaResponseDTO>> listarEmAberto() {
        return ResponseEntity.ok(vendaService.listarVendasEmAberto());
    }

    @GetMapping("/periodo")
    public ResponseEntity<VendasPeriodoDTO> listarPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(vendaService.listarPorPeriodo(inicio, fim));
    }

    @DeleteMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelarVenda(@PathVariable Long id) {
        vendaService.cancelarVenda(id);
        return ResponseEntity.noContent().build();
    }
}
