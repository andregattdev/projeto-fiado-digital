package com.app.Projeto.fiado.digital.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {

    private String token;
    private String tipo;
    private long expiraEm;
    private ComercioResponseDTO comercio;
}
