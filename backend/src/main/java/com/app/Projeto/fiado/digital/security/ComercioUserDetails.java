package com.app.Projeto.fiado.digital.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.app.Projeto.fiado.digital.model.Comercio;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ComercioUserDetails implements UserDetails {

    private final Comercio comercio;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_COMERCIANTE"));
    }

    @Override
    public String getPassword() {
        return comercio.getSenha();
    }

    @Override
    public String getUsername() {
        return comercio.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return comercio.isAtivo();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return comercio.isAtivo();
    }
}
