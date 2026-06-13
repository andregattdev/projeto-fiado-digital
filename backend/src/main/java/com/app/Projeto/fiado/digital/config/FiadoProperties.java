package com.app.Projeto.fiado.digital.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "fiado")
@Getter
@Setter
public class FiadoProperties {

    private int diasVencimento = 30;
}
