package com.app.Projeto.fiado.digital.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.app.Projeto.fiado.digital.dto.AtualizarPerfilDTO;
import com.app.Projeto.fiado.digital.dto.AuthResponseDTO;
import com.app.Projeto.fiado.digital.dto.ComercioResponseDTO;
import com.app.Projeto.fiado.digital.dto.LoginRequestDTO;
import com.app.Projeto.fiado.digital.dto.RegistroComercioDTO;
import com.app.Projeto.fiado.digital.exception.BusinessException;
import com.app.Projeto.fiado.digital.model.Comercio;
import com.app.Projeto.fiado.digital.repository.ComercioRepository;
import com.app.Projeto.fiado.digital.security.JwtService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final ComercioRepository comercioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /** Sempre permite novo cadastro de comércio (cada loja = conta isolada). */
    public boolean registroDisponivel() {
        return true;
    }

    @Transactional
    public AuthResponseDTO registrar(RegistroComercioDTO dto) {
        if (comercioRepository.existsByEmailIgnoreCase(dto.getEmail())) {
            throw new BusinessException("E-mail já cadastrado. Use outro e-mail ou faça login.");
        }

        String cnpjLimpo = null;
        if (dto.getCnpj() != null && !dto.getCnpj().trim().isEmpty()) {
            cnpjLimpo = dto.getCnpj().replaceAll("\\D", "");
            if (!CnpjValidador.isValid(cnpjLimpo)) {
                throw new BusinessException("CNPJ inválido.");
            }
            if (comercioRepository.existsByCnpj(cnpjLimpo)) {
                throw new BusinessException("CNPJ já cadastrado em outro estabelecimento.");
            }
        }

        Comercio comercio = new Comercio();
        comercio.setNomeLoja(dto.getNomeLoja().trim());
        comercio.setNomeResponsavel(dto.getNomeResponsavel().trim());
        comercio.setEmail(dto.getEmail().trim().toLowerCase());
        comercio.setSenha(passwordEncoder.encode(dto.getSenha()));
        comercio.setTelefone(dto.getTelefone() != null ? dto.getTelefone().trim() : null);
        comercio.setCnpj(cnpjLimpo);
        comercio.setAtivo(true);

        comercio = comercioRepository.save(comercio);
        return montarAuthResponse(comercio);
    }

    public AuthResponseDTO login(LoginRequestDTO dto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        dto.getEmail().trim().toLowerCase(),
                        dto.getSenha()));

        Comercio comercio = comercioRepository.findByEmailIgnoreCase(dto.getEmail())
                .orElseThrow(() -> new BusinessException("Credenciais inválidas"));

        return montarAuthResponse(comercio);
    }

    public ComercioResponseDTO comercioLogado(String email) {
        Comercio comercio = comercioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BusinessException("Comércio não encontrado"));
        return toResponse(comercio);
    }

    @Transactional
    public ComercioResponseDTO atualizarPerfil(String email, AtualizarPerfilDTO dto) {
        Comercio comercio = comercioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BusinessException("Comércio não encontrado"));

        comercio.setNomeLoja(dto.getNomeLoja().trim());
        comercio.setNomeResponsavel(dto.getNomeResponsavel().trim());
        
        // CORREÇÃO 1: Evita NullPointerException se o telefone vier em branco ou nulo
        if (dto.getTelefone() != null && !dto.getTelefone().trim().isEmpty()) { 
            comercio.setTelefone(dto.getTelefone().trim());
        } else {
            comercio.setTelefone(null);
        }

        // Validação e Atualização de CNPJ
        String cnpjLimpo = null;
        if (dto.getCnpj() != null && !dto.getCnpj().trim().isEmpty()) {
            cnpjLimpo = dto.getCnpj().replaceAll("\\D", "");
            if (!CnpjValidador.isValid(cnpjLimpo)) {
                throw new BusinessException("CNPJ inválido.");
            }
            if (comercioRepository.existsByCnpjAndIdNot(cnpjLimpo, comercio.getId())) {
                throw new BusinessException("CNPJ já cadastrado em outro estabelecimento.");
            }
        }
        comercio.setCnpj(cnpjLimpo);

        // CORREÇÃO 2: Lógica de atualização opcional da Senha Forte
        if (dto.getNovaSenha() != null && !dto.getNovaSenha().trim().isEmpty()) {
            // Se quer trocar a senha, a senha atual é obrigatória para validação de segurança
            if (dto.getSenhaAtual() == null || dto.getSenhaAtual().trim().isEmpty()) {
                throw new BusinessException("A senha atual é obrigatória para realizar a alteração.");
            }

            // Confere se a senha atual digitada bate com o hash criptografado salvo no banco
            if (!passwordEncoder.matches(dto.getSenhaAtual(), comercio.getSenha())) {
                throw new BusinessException("A senha atual informada está incorreta.");
            }

            // Criptografa e define a nova senha
            comercio.setSenha(passwordEncoder.encode(dto.getNovaSenha()));
        }

        comercio = comercioRepository.save(comercio);
        return toResponse(comercio);
    }

    private AuthResponseDTO montarAuthResponse(Comercio comercio) {
        String token = jwtService.gerarToken(comercio);
        AuthResponseDTO response = new AuthResponseDTO();
        response.setToken(token);
        response.setTipo("Bearer");
        response.setExpiraEm(jwtService.getExpirationMs());
        response.setComercio(toResponse(comercio));
        return response;
    }

    private ComercioResponseDTO toResponse(Comercio comercio) {
        return new ComercioResponseDTO(
                comercio.getId(),
                comercio.getNomeLoja(),
                comercio.getNomeResponsavel(),
                comercio.getEmail(),
                comercio.getTelefone(),
                comercio.getCnpj());
    }
}