package br.mack.estagio.controllers;

import br.mack.estagio.entities.AreaInteresse;
import br.mack.estagio.repositories.AreaInteresseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/areas-interesse")
public class AreaInteresseController {

    @Autowired
    private AreaInteresseRepository repository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AreaInteresse criar(@RequestBody AreaInteresse novaArea) {
        if (novaArea.getNome() == null || novaArea.getNome().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O nome é obrigatório");
        }
        return repository.save(novaArea);
    }

    @GetMapping
    public List<AreaInteresse> lerTudo() {
        return (List<AreaInteresse>) repository.findAll();
    }

    @GetMapping("/{id}")
    public AreaInteresse lerPorId(@PathVariable Long id) {
        Optional<AreaInteresse> optional = repository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Área de Interesse não encontrada");
    }

    @PutMapping("/{id}")
    public AreaInteresse atualizar(@PathVariable Long id, @RequestBody AreaInteresse areaAtualizada) {
        Optional<AreaInteresse> optional = repository.findById(id);
        if (optional.isPresent()) {
            AreaInteresse area = optional.get();
            area.setNome(areaAtualizada.getNome());
            return repository.save(area);
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Área de Interesse não encontrada");
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void apagar(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Área de Interesse não encontrada");
        }
        repository.deleteById(id);
    }
}