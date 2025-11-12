package br.mack.estagio.repositories;

import br.mack.estagio.entities.Estudante;
import org.springframework.data.repository.CrudRepository;

public interface EstudanteRepository extends CrudRepository<Estudante, Long> {
    Estudante findByCpf(String cpf);
}