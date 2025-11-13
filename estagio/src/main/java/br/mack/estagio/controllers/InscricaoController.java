package br.mack.estagio.controllers;

import br.mack.estagio.entities.Estudante;
import br.mack.estagio.entities.Inscricao;
import br.mack.estagio.entities.VagaEstagio;
import br.mack.estagio.repositories.EstudanteRepository;
import br.mack.estagio.repositories.InscricaoRepository;
import br.mack.estagio.repositories.VagaEstagioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/inscricoes")
@Tag(name = "Inscrições", description = "Endpoints para estudantes se inscreverem em vagas")
public class InscricaoController {

    @Autowired
    private InscricaoRepository repository;

    @Autowired
    private EstudanteRepository estudanteRepository;

    @Autowired
    private VagaEstagioRepository vagaEstagioRepository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Realiza a inscrição de um estudante em uma vaga", description = "Cria uma inscrição para o estudante autenticado na vaga especificada. O ID do estudante é obtido do token de segurança.")
    public Inscricao criar(@RequestBody Inscricao novaInscricao) {
        if (novaInscricao.getVagaEstagio() == null || novaInscricao.getVagaEstagio().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O ID da Vaga é obrigatório");
        }

        // Pega o estudante logado a partir do contexto de segurança
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String emailUsuarioLogado = authentication.getName();
        Estudante estudanteLogado = estudanteRepository.findByEmail(emailUsuarioLogado)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuário logado não corresponde a nenhum estudante."));

        VagaEstagio vaga = vagaEstagioRepository.findById(novaInscricao.getVagaEstagio().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vaga não encontrada"));

        // REGRA 1: (Requisito 6) - Garante que a vaga não esteja encerrada.
        // O status é comparado ignorando maiúsculas/minúsculas por segurança.
        if ("ENCERRADA".equalsIgnoreCase(vaga.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Esta vaga não está mais aceitando inscrições.");
        }

        // REGRA 2: Impede que um estudante se inscreva duas vezes na mesma vaga.
        if (repository.existsByEstudanteIdAndVagaEstagioId(estudanteLogado.getId(), vaga.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Você já se inscreveu para esta vaga.");
        }

        novaInscricao.setEstudante(estudanteLogado);
        novaInscricao.setVagaEstagio(vaga);
        novaInscricao.setDataInscricao(new Date()); // Define a data da inscrição

        return repository.save(novaInscricao);
    }

    @GetMapping
    @Operation(summary = "Lista todas as inscrições (Admin)", description = "Retorna uma lista de todas as inscrições. Acesso restrito a administradores.")
    public List<Inscricao> lerTudo() {
        return (List<Inscricao>) repository.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma inscrição por ID (Admin)", description = "Retorna os detalhes de uma inscrição específica. Acesso restrito a administradores.")
    public Inscricao lerPorId(@PathVariable Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inscrição não encontrada"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza o status de uma inscrição (Admin)", description = "Permite que um administrador atualize o status de uma inscrição (ex: 'EM_PROCESSO', 'APROVADO').")
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
    @Operation(summary = "Cancela uma inscrição", description = "Permite que um estudante cancele sua própria inscrição em uma vaga.")
    public void apagar(@PathVariable Long id) {
        // Pega o estudante logado a partir do contexto de segurança
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String emailUsuarioLogado = authentication.getName();
        Estudante estudanteLogado = estudanteRepository.findByEmail(emailUsuarioLogado)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuário logado não corresponde a nenhum estudante."));

        Inscricao inscricao = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inscrição não encontrada"));

        // Valida se o estudante logado é o dono da inscrição
        if (!inscricao.getEstudante().getId().equals(estudanteLogado.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para cancelar esta inscrição.");
        }
        repository.deleteById(id);
    }
}