package com.app.Projeto.fiado.digital.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.Projeto.fiado.digital.dto.AtualizarPerfilDTO;
import com.app.Projeto.fiado.digital.dto.AuthResponseDTO;
import com.app.Projeto.fiado.digital.dto.ComercioResponseDTO;
import com.app.Projeto.fiado.digital.dto.LoginRequestDTO;
import com.app.Projeto.fiado.digital.dto.RegistroComercioDTO;
import com.app.Projeto.fiado.digital.security.ComercioUserDetails;
import com.app.Projeto.fiado.digital.security.JwtService;
import com.app.Projeto.fiado.digital.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    @GetMapping("/registro-disponivel")
    public ResponseEntity<Map<String, Boolean>> registroDisponivel() {
        return ResponseEntity.ok(Map.of("disponivel", authService.registroDisponivel()));
    }

    @PostMapping("/registrar")
    public ResponseEntity<AuthResponseDTO> registrar(@Valid @RequestBody RegistroComercioDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    @GetMapping("/me")
    public ResponseEntity<ComercioResponseDTO> me(@AuthenticationPrincipal ComercioUserDetails user) {
        return ResponseEntity.ok(authService.comercioLogado(user.getUsername()));
    }

    @PutMapping("/perfil")
    public ResponseEntity<ComercioResponseDTO> atualizarPerfil(
            @RequestHeader("Authorization") String tokenHeader, // 👈 Pega o token vindo do Angular
            @Valid @RequestBody AtualizarPerfilDTO dto) {

        // Remove o prefixo "Bearer " para deixar apenas o hash puro do token
        String token = tokenHeader.substring(7);

        // Usa o método que você acabou de achar para pegar o e-mail de forma segura
        String email = jwtService.extrairEmail(token);

        // Executa a atualização usando o e-mail recuperado
        return ResponseEntity.ok(authService.atualizarPerfil(email, dto));
    }
}
