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

import br.mack.estagio.entities.Empresa;
import br.mack.estagio.repositories.EmpresaRepository;

@RestController
@RequestMapping("/empresas")
public class EmpresaController {
    @Autowired
    private EmpresaRepository repository;

    //CREATE
    @PostMapping
    public Empresa criarEmpresa(@RequestBody Empresa novaEmpresa) {
        if (novaEmpresa.getNome() == null || novaEmpresa.getNome().isEmpty() ||
            novaEmpresa.getCnpj() == null || novaEmpresa.getCnpj().isEmpty() ||
            novaEmpresa.getEmail() == null || novaEmpresa.getEmail().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome, CNPJ e Email são obrigatórios");
        }
        return repository.save(novaEmpresa);
    }

    //READ
    @GetMapping
    public List<Empresa> lerTodasEmpresas(@RequestParam(name="nome", required=false) String nome) {
        if(nome == null) {
            return (List<Empresa>) repository.findAll();
        }
        return repository.findByNome(nome);
    }

    @GetMapping("/{id}")
    public Empresa lerEmpresaPeloId(@PathVariable long id) {
        Optional<Empresa> optional = repository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada");
    }

    //UPDATE
    @PutMapping("/{id}")
    public Empresa atualizarEmpresaPeloId(
        @RequestBody Empresa novosDados, 
        @PathVariable long id) {
        
        Optional<Empresa> optional = repository.findById(id);
        if (optional.isPresent()) {
            Empresa empresa = optional.get();
            empresa.setNome(novosDados.getNome());
            empresa.setCnpj(novosDados.getCnpj());
            empresa.setEmail(novosDados.getEmail());
            empresa.setTelefone(novosDados.getTelefone());
            empresa.setEndereco(novosDados.getEndereco());
            empresa.setAreasAtuacao(novosDados.getAreasAtuacao());
            return repository.save(empresa);
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada");
    }

    //DELETE
    @DeleteMapping("/{id}")
    public void apagarPeloId(@PathVariable long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada");
        }
        repository.deleteById(id);
    }


}