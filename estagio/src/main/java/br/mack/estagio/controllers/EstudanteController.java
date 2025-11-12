package br.mack.estagio.controllers;

import br.mack.estagio.entities.Estudante;
import br.mack.estagio.repositories.EstudanteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/estudantes")
public class EstudanteController {

    @Autowired
    private EstudanteRepository estudanteRepository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Estudante criarEstudante(@RequestBody Estudante estudante) {
        if (estudante.getNome() == null || estudante.getNome().isEmpty() ||
            estudante.getCpf() == null || estudante.getCpf().isEmpty() ||
            estudante.getEmail() == null || estudante.getEmail().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome, CPF e Email são obrigatórios");
        }
        return estudanteRepository.save(estudante);
    }

    @GetMapping
    public List<Estudante> lerTodosEstudantes() {
        return (List<Estudante>) estudanteRepository.findAll();
    }

    @GetMapping("/{id}")
    public Estudante lerEstudantePorId(@PathVariable Long id) {
        Optional<Estudante> optional = estudanteRepository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Estudante não encontrado");
    }

    @GetMapping("/cpf/{cpf}")
    public Estudante lerEstudantePorCpf(@PathVariable String cpf) {
        Estudante estudante = estudanteRepository.findByCpf(cpf);
        if (estudante == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Estudante não encontrado com este CPF");
        }
        return estudante;
    }

    @PutMapping("/{id}")
    public Estudante atualizarEstudante(@PathVariable Long id, @RequestBody Estudante estudanteAtualizado) {
        Optional<Estudante> optional = estudanteRepository.findById(id);
        if (optional.isPresent()) {
            Estudante estudante = optional.get();
            estudante.setNome(estudanteAtualizado.getNome());
            estudante.setCpf(estudanteAtualizado.getCpf());
            estudante.setEmail(estudanteAtualizado.getEmail());
            estudante.setCurso(estudanteAtualizado.getCurso());
            estudante.setTelefone(estudanteAtualizado.getTelefone());
            estudante.setListAreaInteresse(estudanteAtualizado.getListAreaInteresse());
            return estudanteRepository.save(estudante);
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Estudante não encontrado");
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletarEstudante(@PathVariable Long id) {
        if (!estudanteRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Estudante não encontrado");
        }
        estudanteRepository.deleteById(id);
    }
}