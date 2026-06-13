package com.app.Projeto.fiado.digital.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "clientes", uniqueConstraints = {
        @UniqueConstraint(name = "uk_cliente_cpf_por_comercio", columnNames = { "cpf", "comercio_id" })
})
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String cpf;

    @ManyToOne(optional = false)
    @JoinColumn(name = "comercio_id", nullable = false)
    private Comercio comercio;

    private String telefone;

    private String endereco;

    private LocalDate dataNascimento;

    @Transient
    private BigDecimal totalDevido = BigDecimal.ZERO;

    @OneToMany(mappedBy = "cliente")
    private List<Venda> vendas = new ArrayList<>();
}
