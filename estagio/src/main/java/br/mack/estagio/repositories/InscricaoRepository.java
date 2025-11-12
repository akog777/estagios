package br.mack.estagio.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import br.mack.estagio.entities.Inscricao;

public interface InscricaoRepository extends CrudRepository<Inscricao, Long>{

    List<Inscricao> findByVagaEstagioId(Long vagaId);
    List<Inscricao> findByEstudanteId(Long estudanteId);
    boolean existsByEstudanteIdAndVagaEstagioId(Long estudanteId, Long vagaId);
    List<Inscricao> findByVagaEstagioEmpresaId(Long empresaId);
}