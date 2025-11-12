package br.mack.estagio.controllers;

import br.mack.estagio.entities.Estudante;
import br.mack.estagio.entities.Inscricao;
import br.mack.estagio.entities.VagaEstagio;
import br.mack.estagio.repositories.EstudanteRepository;
import br.mack.estagio.repositories.InscricaoRepository;
import br.mack.estagio.repositories.VagaEstagioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/inscricoes")
public class InscricaoController {

    @Autowired
    private InscricaoRepository repository;

    @Autowired
    private EstudanteRepository estudanteRepository;

    @Autowired
    private VagaEstagioRepository vagaEstagioRepository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Inscricao criar(@RequestBody Inscricao novaInscricao) {
        if (novaInscricao.getEstudante() == null || novaInscricao.getEstudante().getId() == null ||
            novaInscricao.getVagaEstagio() == null || novaInscricao.getVagaEstagio().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID do Estudante e da Vaga são obrigatórios");
        }

        // Valida se as entidades relacionadas existem
        Estudante estudante = estudanteRepository.findById(novaInscricao.getEstudante().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estudante não encontrado"));
        VagaEstagio vaga = vagaEstagioRepository.findById(novaInscricao.getVagaEstagio().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vaga não encontrada"));

        // REGRA 1: (Requisito 6) - Garante que a vaga não esteja encerrada.
        // O status é comparado ignorando maiúsculas/minúsculas por segurança.
        if ("ENCERRADA".equalsIgnoreCase(vaga.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Esta vaga não está mais aceitando inscrições.");
        }

        // REGRA 2: Impede que um estudante se inscreva duas vezes na mesma vaga.
        if (repository.existsByEstudanteIdAndVagaEstagioId(estudante.getId(), vaga.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Você já se inscreveu para esta vaga.");
        }

        novaInscricao.setEstudante(estudante);
        novaInscricao.setVagaEstagio(vaga);
        novaInscricao.setDataInscricao(new Date()); // Define a data da inscrição

        return repository.save(novaInscricao);
    }

    @GetMapping
    public List<Inscricao> lerTudo() {
        return (List<Inscricao>) repository.findAll();
    }

    @GetMapping("/{id}")
    public Inscricao lerPorId(@PathVariable Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inscrição não encontrada"));
    }

    @PutMapping("/{id}")
    public Inscricao atualizar(@PathVariable Long id, @RequestBody Inscricao inscricaoAtualizada) {
        Inscricao inscricao = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inscrição não encontrada"));

        // Geralmente, apenas o status da inscrição é atualizado.
        // O estudante e a vaga não devem ser alterados.
        inscricao.setStatus(inscricaoAtualizada.getStatus());

        return repository.save(inscricao);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void apagar(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Inscrição não encontrada");
        }
        repository.deleteById(id);
    }
}