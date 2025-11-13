package br.mack.estagio.entities;

import java.util.Date;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VagaEstagio {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String titulo;
    private String descricao;
    private String localizacao;
    private String modalidade; // remoto/presencial/híbrido
    private int cargaHoraria;
    private String requisitos;
    private String status; // ABERTA, ENCERRADA

    @ManyToMany
    private List<AreaInteresse> listAreaInteresse;

    @ManyToOne
    private Empresa empresa;

    @OneToMany(mappedBy = "vagaEstagio")
    private List<Inscricao> inscricoes;
}