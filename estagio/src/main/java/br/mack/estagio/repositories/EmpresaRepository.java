package br.mack.estagio.repositories;

import br.mack.estagio.entities.Empresa;
import org.springframework.data.repository.CrudRepository;
import java.util.List;
import java.util.Optional;

public interface EmpresaRepository extends CrudRepository<Empresa, Long> {
    Optional<Empresa> findByEmail(String email);
    
    List<Empresa> findByNome(String nome);
}