package br.mack.estagio.controllers;

import br.mack.estagio.entities.Estudante;
import br.mack.estagio.entities.Usuario;
import br.mack.estagio.repositories.EstudanteRepository;
import br.mack.estagio.repositories.UsuarioRepository;
import br.mack.estagio.entities.VagaEstagio;
import br.mack.estagio.repositories.VagaEstagioRepository;
import br.mack.estagio.services.PdfGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private UsuarioRepository usuarioRepository;

    @Autowired
    private VagaEstagioRepository vagaEstagioRepository;

    @Autowired
    private PdfGenerationService pdfGenerationService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra um novo estudante", description = "Cria um novo estudante e um usuário associado para login. A senha deve ser informada no campo 'senha' do JSON.")
    public Estudante criarEstudante(@RequestBody Estudante estudante) {
        if (estudante.getNome() == null || estudante.getNome().isEmpty() ||
                estudante.getCpf() == null || estudante.getCpf().isEmpty() ||
                estudante.getEmail() == null || estudante.getEmail().isEmpty() ||
                estudante.getSenha() == null || estudante.getSenha().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome, CPF, Email e Senha são obrigatórios");
        }

        // 1. Verifica se o e-mail (que será o login) já está em uso
        usuarioRepository.findByEmail(estudante.getEmail()).ifPresent(user -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este e-mail já está cadastrado.");
        });

        // 2. Cria e salva o novo usuário com perfil de ESTUDANTE
        Usuario novoUsuario = new Usuario();
        novoUsuario.setEmail(estudante.getEmail());
        novoUsuario.setSenha(passwordEncoder.encode(estudante.getSenha())); // Criptografa a senha
        novoUsuario.setRole("ROLE_ESTUDANTE");
        usuarioRepository.save(novoUsuario);

        // 3. Associa o usuário ao estudante e salva a entidade Estudante
        estudante.setUsuario(novoUsuario);
        Estudante estudanteSalvo = estudanteRepository.save(estudante);

        estudanteSalvo.setSenha(null); // Garante que a senha não seja retornada na resposta
        return estudanteSalvo;
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
        Estudante estudanteLogado = getEstudanteLogado();
        if (!estudanteLogado.getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você só pode atualizar seu próprio perfil.");
        }

        // Atualiza os dados do estudante logado com as informações recebidas
        estudanteLogado.setNome(estudanteAtualizado.getNome());
        estudanteLogado.setCpf(estudanteAtualizado.getCpf());
        estudanteLogado.setEmail(estudanteAtualizado.getEmail());
        estudanteLogado.setCurso(estudanteAtualizado.getCurso());
        estudanteLogado.setTelefone(estudanteAtualizado.getTelefone());
        estudanteLogado.setListAreaInteresse(estudanteAtualizado.getListAreaInteresse());
        return estudanteRepository.save(estudanteLogado);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletarEstudante(@PathVariable Long id) {
        Estudante estudanteLogado = getEstudanteLogado();
        if (!estudanteLogado.getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você só pode deletar seu próprio perfil.");
        }
        estudanteRepository.deleteById(id);
    }

    // REGRA: (Requisito 7) - Endpoint para o estudante logado ver vagas recomendadas.
    @GetMapping("/me/vagas-recomendadas")
    public List<VagaEstagio> verVagasRecomendadas() {
        Estudante estudanteLogado = getEstudanteLogado();

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

    private Estudante getEstudanteLogado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String emailUsuarioLogado = authentication.getName();
        return estudanteRepository.findByEmail(emailUsuarioLogado)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Nenhum estudante encontrado para o usuário logado."));
    }
}