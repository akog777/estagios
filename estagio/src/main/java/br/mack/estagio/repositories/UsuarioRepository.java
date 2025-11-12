package br.mack.estagio.repositories;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import br.mack.estagio.entities.Usuario;

public interface UsuarioRepository extends CrudRepository<Usuario, Long> {
    Optional<Usuario> findByLogin(String login);
}