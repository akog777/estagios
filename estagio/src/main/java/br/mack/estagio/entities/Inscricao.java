package br.mack.estagio.entities;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inscricao {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Date dataInscricao;
    private String status; // Ex: "INSCRITO", "EM_PROCESSO", "APROVADO", "REJEITADO"

    @ManyToOne
    private VagaEstagio vagaEstagio;

    @ManyToOne
    private Estudante estudante;
}