package com.app.Projeto.fiado.digital.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.app.Projeto.fiado.digital.exception.BusinessException;
import com.app.Projeto.fiado.digital.model.Comercio;

@Service
public class ComercioContextService {

    public Comercio getComercioLogado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof ComercioUserDetails details) {
            return details.getComercio();
        }
        throw new BusinessException("Sessão do comércio inválida. Faça login novamente.");
    }

    public Long getComercioIdLogado() {
        return getComercioLogado().getId();
    }
}
