package br.mack.estagio.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import br.mack.estagio.entities.Empresa;

public interface EmpresaRepository extends CrudRepository<Empresa, Long>{
    List<Empresa> findByNome(String nome);
}