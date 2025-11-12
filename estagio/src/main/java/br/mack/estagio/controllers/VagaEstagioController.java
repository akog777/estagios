package br.mack.estagio.controllers;

import br.mack.estagio.entities.VagaEstagio;
import br.mack.estagio.entities.Empresa;
import br.mack.estagio.repositories.VagaEstagioRepository;
import br.mack.estagio.repositories.EmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/vagas")
@Tag(name = "Vagas de Estágio", description = "Endpoints para gerenciamento de vagas de estágio")
public class VagaEstagioController {

    @Autowired
    private VagaEstagioRepository repository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria uma nova vaga de estágio", description = "Cria uma nova vaga. O sistema associará a vaga à empresa autenticada. O status inicial será 'ABERTA'.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Vaga criada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado. O usuário não tem o perfil 'EMPRESA'.")
            @ApiResponse(responseCode = "403", description = "Acesso negado. O usuário não tem o perfil 'EMPRESA' ou não corresponde a uma empresa cadastrada.")
    })
    public VagaEstagio criar(@RequestBody VagaEstagio novaVaga) {
        if (novaVaga.getTitulo() == null || novaVaga.getTitulo().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O título da vaga é obrigatório.");
        }

        // Pega o email da empresa logada a partir do contexto de segurança
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String emailUsuarioLogado = authentication.getName();
        Empresa empresaLogada = empresaRepository.findByEmail(emailUsuarioLogado)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuário logado não corresponde a nenhuma empresa."));

        // Associa a vaga à empresa que está autenticada, garantindo a segurança.
        novaVaga.setEmpresa(empresaLogada);
        // REGRA 1: (Requisito 5) - Garante que toda nova vaga comece com o status "ABERTA".
        novaVaga.setStatus("ABERTA");

        return repository.save(novaVaga);
    }

    @GetMapping
    @Operation(summary = "Lista todas as vagas de estágio", description = "Este endpoint é público e retorna uma lista de todas as vagas cadastradas.")
    public List<VagaEstagio> lerTudo() {
        return (List<VagaEstagio>) repository.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma vaga por ID", description = "Retorna os detalhes de uma vaga específica. Este endpoint é público.")
    public VagaEstagio lerPorId(@PathVariable Long id) {
        Optional<VagaEstagio> optional = repository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vaga de estágio não encontrada");
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma vaga existente", description = "Permite que a empresa dona da vaga atualize suas informações.")
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

    // REGRA 2: (Requisito 8) - Endpoint para a empresa encerrar uma vaga.
    @PatchMapping("/{id}/encerrar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Encerra uma vaga", description = "Altera o status de uma vaga para 'ENCERRADA', impedindo novas inscrições.")
    public void encerrarVaga(@PathVariable Long id) {
        // Busca a vaga no banco de dados. Se não encontrar, lança um erro 404.
        VagaEstagio vaga = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vaga de estágio não encontrada"));

        // Altera o status da vaga para "ENCERRADA".
        vaga.setStatus("ENCERRADA");

        // Salva a alteração no banco de dados.
        repository.save(vaga);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deleta uma vaga", description = "Remove permanentemente o registro de uma vaga. Apenas para empresas.")
    public void apagar(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vaga de estágio não encontrada");
        }
        repository.deleteById(id);
    }
}