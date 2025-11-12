package br.mack.estagio.controllers;

import br.mack.estagio.entities.VagaEstagio;
import br.mack.estagio.repositories.VagaEstagioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/vagas")
public class VagaEstagioController {

    @Autowired
    private VagaEstagioRepository repository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VagaEstagio criar(@RequestBody VagaEstagio novaVaga) {
        if (novaVaga.getTitulo() == null || novaVaga.getTitulo().isEmpty() ||
            novaVaga.getEmpresa() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Título e Empresa são obrigatórios");
        }
        return repository.save(novaVaga);
    }

    @GetMapping
    public List<VagaEstagio> lerTudo() {
        return (List<VagaEstagio>) repository.findAll();
    }

    @GetMapping("/{id}")
    public VagaEstagio lerPorId(@PathVariable Long id) {
        Optional<VagaEstagio> optional = repository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vaga de estágio não encontrada");
    }

    @PutMapping("/{id}")
    public VagaEstagio atualizar(@PathVariable Long id, @RequestBody VagaEstagio vagaAtualizada) {
        Optional<VagaEstagio> optional = repository.findById(id);
        if (optional.isPresent()) {
            VagaEstagio vaga = optional.get();
            vaga.setTitulo(vagaAtualizada.getTitulo());
            vaga.setDescricao(vagaAtualizada.getDescricao());
            vaga.setLocalizacao(vagaAtualizada.getLocalizacao());
            vaga.setModalidade(vagaAtualizada.getModalidade());
            vaga.setCargaHoraria(vagaAtualizada.getCargaHoraria());
            vaga.setRequisitos(vagaAtualizada.getRequisitos());
            vaga.setStatus(vagaAtualizada.getStatus());
            vaga.setListAreaInteresse(vagaAtualizada.getListAreaInteresse());
            vaga.setEmpresa(vagaAtualizada.getEmpresa());
            // As datas também podem ser atualizadas se necessário
            // vaga.setDataInicio(vagaAtualizada.getDataInicio());
            // vaga.setDataFim(vagaAtualizada.getDataFim());
            return repository.save(vaga);
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vaga de estágio não encontrada");
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void apagar(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vaga de estágio não encontrada");
        }
        repository.deleteById(id);
    }
}