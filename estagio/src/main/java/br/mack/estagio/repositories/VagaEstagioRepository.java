package br.mack.estagio.repositories;

import br.mack.estagio.entities.VagaEstagio;
import org.springframework.data.repository.CrudRepository;

public interface VagaEstagioRepository extends CrudRepository<VagaEstagio, Long> {
    Long countByStatus(String status);
}