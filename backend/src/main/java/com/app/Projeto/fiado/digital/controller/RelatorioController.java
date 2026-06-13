package com.app.Projeto.fiado.digital.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.Projeto.fiado.digital.dto.DividaVencidaDTO;
import com.app.Projeto.fiado.digital.dto.RelatorioMensalDTO;
import com.app.Projeto.fiado.digital.service.RelatorioService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/relatorios")
@RequiredArgsConstructor
public class RelatorioController {

    private final RelatorioService relatorioService;

    @GetMapping("/mensal")
    public ResponseEntity<RelatorioMensalDTO> relatorioMensal(
            @RequestParam int mes,
            @RequestParam int ano) {
        return ResponseEntity.ok(relatorioService.gerarRelatorioMensal(mes, ano));
    }

    @GetMapping("/dividas-vencidas")
    public ResponseEntity<List<DividaVencidaDTO>> dividasVencidas(
            @RequestParam(required = false) Integer dias) {
        return ResponseEntity.ok(relatorioService.listarDividasVencidas(dias));
    }
}
