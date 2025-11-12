package br.mack.estagio.repositories;

import br.mack.estagio.entities.Estudante;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface EstudanteRepository extends CrudRepository<Estudante, Long> {
    Estudante findByCpf(String cpf);
    Optional<Estudante> findByEmail(String email);
}