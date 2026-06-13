package com.app.Projeto.fiado.digital.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.Projeto.fiado.digital.dto.PagamentoDTO;
import com.app.Projeto.fiado.digital.service.PagamentoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/pagamentos")
@RequiredArgsConstructor
public class PagamentoController {

    private final PagamentoService pagamentoService;

    @PostMapping
    public ResponseEntity<PagamentoDTO> registrar( @Valid @RequestBody PagamentoDTO dto) {
        PagamentoDTO salvo = pagamentoService.registrarPagamento(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    @GetMapping("/venda/{vendaId}")
    public ResponseEntity<List<PagamentoDTO>> listarPorVenda(@PathVariable Long vendaId) {
        return ResponseEntity.ok(pagamentoService.listarPagamentosDaVenda(vendaId));
    }
}
