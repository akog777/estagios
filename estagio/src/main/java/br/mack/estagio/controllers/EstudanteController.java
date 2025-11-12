package br.mack.estagio.controllers;

import br.mack.estagio.entities.Estudante;
import br.mack.estagio.repositories.EstudanteRepository;
import br.mack.estagio.entities.VagaEstagio;
import br.mack.estagio.repositories.VagaEstagioRepository;
import br.mack.estagio.services.PdfGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/estudantes")
public class EstudanteController {

    @Autowired
    private EstudanteRepository estudanteRepository;

    @Autowired
    private VagaEstagioRepository vagaEstagioRepository;

    @Autowired
    private PdfGenerationService pdfGenerationService;

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

    // REGRA: (Requisito 7) - Endpoint para o estudante logado ver vagas recomendadas.
    @GetMapping("/me/vagas-recomendadas")
    public List<VagaEstagio> verVagasRecomendadas() {
        // 1. Pega os dados do usuário autenticado.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String emailUsuarioLogado = authentication.getName();

        // 2. Busca a entidade Estudante correspondente ao email do usuário logado.
        Estudante estudanteLogado = estudanteRepository.findByEmail(emailUsuarioLogado)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nenhum estudante encontrado para o usuário logado."));

        // 3. Se o estudante não tiver áreas de interesse, retorna uma lista vazia.
        if (estudanteLogado.getListAreaInteresse() == null || estudanteLogado.getListAreaInteresse().isEmpty()) {
            return Collections.emptyList();
        }

        // 4. Usa as áreas de interesse do estudante para buscar vagas abertas correspondentes.
        return vagaEstagioRepository.findDistinctByStatusAndListAreaInteresseIn("ABERTA", estudanteLogado.getListAreaInteresse());
    }

    // FUNCIONALIDADE INOVADORA: Gera o currículo do estudante em PDF.
    @GetMapping("/{id}/curriculo")
    public ResponseEntity<byte[]> getCurriculoPdf(@PathVariable Long id) {
        // Busca o estudante pelo ID
        Estudante estudante = estudanteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estudante não encontrado"));

        // Gera o PDF usando o serviço
        byte[] pdfBytes = pdfGenerationService.generateCurriculoPdf(estudante);

        // Configura os cabeçalhos da resposta para indicar que é um arquivo PDF
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        // O nome do arquivo que será sugerido no download
        headers.setContentDispositionFormData("filename", "curriculo-" + estudante.getNome().replace(" ", "_") + ".pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}