package com.app.Projeto.fiado.digital.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.app.Projeto.fiado.digital.repository.ComercioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ComercioUserDetailsService implements UserDetailsService {

    private final ComercioRepository comercioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return comercioRepository.findByEmailIgnoreCase(username)
                .map(ComercioUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("Comércio não encontrado"));
    }
}
