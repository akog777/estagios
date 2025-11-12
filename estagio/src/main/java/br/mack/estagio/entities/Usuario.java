package br.mack.estagio.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    @Id @GeneratedValue
    private Long id;
    private String email;
    private String senha;
    private String role; // Ex: "ROLE_ADMIN", "ROLE_EMPRESA", "ROLE_ESTUDANTE"

}