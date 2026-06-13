package com.app.Projeto.fiado.digital.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ComercioResponseDTO {

    private Long id;
    private String nomeLoja;
    private String nomeResponsavel;
    private String email;
    private String telefone;
    private String cnpj;
}
