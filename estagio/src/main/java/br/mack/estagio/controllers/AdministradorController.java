package br.mack.estagio.controllers;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import br.mack.estagio.entities.Administrador;
import br.mack.estagio.dtos.VagasPorAreaDTO;
import br.mack.estagio.repositories.AdministradorRepository;
import br.mack.estagio.repositories.EmpresaRepository;
import br.mack.estagio.repositories.EstudanteRepository;
import br.mack.estagio.repositories.VagaEstagioRepository;

@RestController
@RequestMapping("/admins")
public class AdministradorController {
    @Autowired
    private AdministradorRepository repository;
    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private EstudanteRepository estudanteRepository;
    @Autowired
    private VagaEstagioRepository vagaEstagioRepository;

    //CREATE
    @PostMapping
    public Administrador criarUsuario(@RequestBody Administrador novoAdmin) {
        if( novoAdmin.getNome() == null ||
            novoAdmin.getEmail() == null ||
            novoAdmin.getNome().isEmpty() || 
            novoAdmin.getEmail().isEmpty()) {
                 throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        return repository.save(novoAdmin);
    }

    //READ
    @GetMapping
    public List<Administrador> lerTodosUsuarios(@RequestParam(name="nome", required=false) String nome) {
        if(nome == null){
            return (List<Administrador>) repository.findAll();
        }

        return repository.findByNome(nome);
    }

    @GetMapping("/{id}")
    public Administrador lerUsuarioPeloId(@PathVariable long id) {
        Optional<Administrador> optional = repository.findById(id);
        
        if(optional.isPresent()) return optional.get();

        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    //UPDATE
    @PutMapping("/{id}")
    public Administrador atualizarUsuarioPeloId(
        @RequestBody Administrador novosDados, 
        @PathVariable long id) {
        
        Optional<Administrador> optional = repository.findById(id);
        if(optional.isPresent()) {
            Administrador u = optional.get();
            u.setNome(novosDados.getNome());
            u.setEmail(novosDados.getEmail());
            return repository.save(u);
        } 

        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    //DELETE
    @DeleteMapping("/{id}")
    public void apagarPeloId(@PathVariable long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Administrador não encontrado");
        }
        repository.deleteById(id);
    }

    // REGRA 1: (Requisito 9) - Endpoint para o dashboard administrativo.
    @GetMapping("/dashboard")
    public Map<String, Long> getDashboardStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("quantidadeEmpresas", empresaRepository.count());
        stats.put("quantidadeEstudantes", estudanteRepository.count());
        stats.put("totalVagas", vagaEstagioRepository.count());
        stats.put("vagasAbertas", vagaEstagioRepository.countByStatus("ABERTA"));
        stats.put("vagasEncerradas", vagaEstagioRepository.countByStatus("ENCERRADA"));
        return stats;
    }

    // REGRA 2: (Requisito 9) - Endpoint para dados do gráfico de vagas por área.
    @GetMapping("/dashboard/vagas-por-area")
    public List<VagasPorAreaDTO> getVagasPorArea() {
        return vagaEstagioRepository.countVagasByArea();
    }

}