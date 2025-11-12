package br.mack.estagio.repositories;

import br.mack.estagio.entities.VagaEstagio;
import br.mack.estagio.entities.AreaInteresse;
import br.mack.estagio.entities.Empresa;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import br.mack.estagio.dtos.VagasPorAreaDTO;

public interface VagaEstagioRepository extends CrudRepository<VagaEstagio, Long> {
    Long countByStatus(String status);
    List<VagaEstagio> findDistinctByStatusAndListAreaInteresseIn(String status, List<AreaInteresse> areas);
    List<VagaEstagio> findByEmpresa(Empresa empresa);
    @Query("SELECT new br.mack.estagio.dtos.VagasPorAreaDTO(ai.nome, COUNT(DISTINCT v)) FROM VagaEstagio v JOIN v.listAreaInteresse ai GROUP BY ai.nome")
    List<VagasPorAreaDTO> countVagasByArea();
}