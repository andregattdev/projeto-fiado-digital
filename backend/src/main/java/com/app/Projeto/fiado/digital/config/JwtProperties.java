package com.app.Projeto.fiado.digital.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {

    private String secret = "ALTERE-ESTA-CHAVE-SECRETA-EM-PRODUCAO-MINIMO-32-CARACTERES";
    private long expirationMs = 86_400_000L;
}
