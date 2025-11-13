package br.mack.estagio.controllers;

import br.mack.estagio.entities.Empresa;
import br.mack.estagio.entities.Inscricao;
import br.mack.estagio.entities.Usuario;
import br.mack.estagio.entities.VagaEstagio;
import br.mack.estagio.repositories.EmpresaRepository;
import br.mack.estagio.repositories.UsuarioRepository;
import br.mack.estagio.repositories.VagaEstagioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/empresas")
@Tag(name = "Empresas", description = "Endpoints para cadastro e gerenciamento de empresas")
public class EmpresaController {

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private VagaEstagioRepository vagaEstagioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/registrar")
    @ResponseStatus(HttpStatus.CREATED)
    public Empresa registrarEmpresa(@RequestBody Empresa novaEmpresa) {
        if (novaEmpresa.getNome() == null || novaEmpresa.getNome().isEmpty() ||
                novaEmpresa.getCnpj() == null || novaEmpresa.getCnpj().isEmpty() ||
                novaEmpresa.getUsuario() == null ||
                novaEmpresa.getUsuario().getEmail() == null || novaEmpresa.getUsuario().getEmail().isEmpty() ||
                novaEmpresa.getUsuario().getSenha() == null || novaEmpresa.getUsuario().getSenha().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome, CNPJ, Email e Senha são obrigatórios");
        }

        // Verifica se o e-mail já está em uso
        usuarioRepository.findByEmail(novaEmpresa.getUsuario().getEmail()).ifPresent(user -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este e-mail já está cadastrado.");
        });

        // Cria e salva o novo usuário com perfil de EMPRESA
        Usuario novoUsuario = new Usuario();
        novoUsuario.setEmail(novaEmpresa.getUsuario().getEmail());
        novoUsuario.setSenha(passwordEncoder.encode(novaEmpresa.getUsuario().getSenha()));
        novoUsuario.setRole("ROLE_EMPRESA");
        usuarioRepository.save(novoUsuario);

        // Associa o usuário à empresa e salva
        novaEmpresa.setUsuario(novoUsuario);
        Empresa empresaSalva = empresaRepository.save(novaEmpresa);

        // Não retorna a senha
        empresaSalva.getUsuario().setSenha(null);
        return empresaSalva;
    }

    @GetMapping("/me")
    @Operation(summary = "Busca o perfil da empresa logada", description = "Retorna os dados cadastrais da empresa que está autenticada.")
    public Empresa getMyProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Se a segurança estiver desabilitada para teste, não podemos pegar o usuário logado.
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Segurança desabilitada ou usuário não autenticado. Não é possível usar o /me");
        }
        String emailUsuarioLogado = authentication.getName();

        return empresaRepository.findByUsuarioEmail(emailUsuarioLogado)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil de empresa não encontrado para o usuário logado."));
    }

    @PutMapping("/me")
    @Operation(summary = "Atualiza o perfil da empresa logada", description = "Permite que a empresa autenticada atualize seus dados cadastrais.")
    public Empresa updateMyProfile(@RequestBody Empresa empresaAtualizada) {
        Empresa empresa = getMyProfile(); // Reutiliza o método acima para buscar a empresa logada

        // Atualiza os campos permitidos
        empresa.setNome(empresaAtualizada.getNome());
        empresa.setCnpj(empresaAtualizada.getCnpj());
        empresa.setTelefone(empresaAtualizada.getTelefone());
        empresa.setEndereco(empresaAtualizada.getEndereco());
        empresa.setAreasAtuacao(empresaAtualizada.getAreasAtuacao());

        return empresaRepository.save(empresa);
    }

    @GetMapping
    @Operation(summary = "Lista todas as empresas (Admin)", description = "Retorna uma lista de todas as empresas cadastradas. Acesso restrito a administradores.")
    public List<Empresa> listarTodas() {
        return (List<Empresa>) empresaRepository.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca empresa por ID (Admin)", description = "Retorna os dados de uma empresa específica. Acesso restrito a administradores.")
    public Empresa buscarPorId(@PathVariable Long id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada"));
    }

    // REGRA: (Requisito 7) - Endpoint para a empresa logada ver todos os seus candidatos.
    @GetMapping("/me/candidatos")
    @Operation(summary = "Lista todos os candidatos inscritos nas vagas da empresa logada", description = "Retorna uma lista de todas as inscrições ativas para as vagas da empresa autenticada, ideal para o painel principal.")
    public List<Inscricao> getMyCandidates() {
        // 1. Reutiliza o método que busca a empresa logada
        Empresa empresaLogada = getMyProfile();

        // 2. Busca todas as vagas associadas a essa empresa
        List<VagaEstagio> minhasVagas = vagaEstagioRepository.findByEmpresa(empresaLogada);

        // 3. Usa Streams para extrair e juntar todas as inscrições de todas as vagas em uma única lista
        return minhasVagas.stream()
                .flatMap(vaga -> vaga.getInscricoes().stream())
                .collect(Collectors.toList());
    }

    // Endpoint para listar vagas da empresa logada
    @GetMapping("/me/vagas")
    @Operation(summary = "Lista todas as vagas da empresa logada", description = "Retorna uma lista de todas as vagas criadas pela empresa autenticada.")
    public List<VagaEstagio> getMyVagas() {
        Empresa empresaLogada = getMyProfile();
        return vagaEstagioRepository.findByEmpresa(empresaLogada);
    }
}