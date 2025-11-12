package br.mack.estagio.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import br.mack.estagio.entities.Administrador;

public interface AdministradorRepository extends CrudRepository<Administrador, Long> {
    List<Administrador> findByNome(String nome);
}