package com.app.Projeto.fiado.digital.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VendaRequestDTO {

    private Long clienteId;
    private List<VendaItemDTO> itens;
    private String observacao;
}
