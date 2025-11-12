package br.mack.estagio.repositories;

import org.springframework.data.repository.CrudRepository;

import br.mack.estagio.entities.Empresa;
import br.mack.estagio.entities.Usuario;

public interface UsuarioRepository extends CrudRepository<Usuario, Long>{

}