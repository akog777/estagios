package br.mack.estagio.controllers;

import br.mack.estagio.entities.Empresa;
import br.mack.estagio.entities.Usuario;
import br.mack.estagio.repositories.EmpresaRepository;
import br.mack.estagio.repositories.UsuarioRepository;
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

@RestController
@RequestMapping("/empresas")
@Tag(name = "Empresas", description = "Endpoints para cadastro e gerenciamento de empresas")
public class EmpresaController {

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/registrar")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra uma nova empresa no sistema", description = "Cria um usuário com perfil 'EMPRESA' e associa a uma nova entidade Empresa com todos os seus dados.")
    public Empresa registrar(@RequestBody Empresa novaEmpresa) {
        // Validação básica
        if (novaEmpresa.getUsuario() == null || novaEmpresa.getUsuario().getSenha() == null || novaEmpresa.getUsuario().getEmail() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email e senha são obrigatórios para o registro.");
        }
        if (usuarioRepository.findByEmail(novaEmpresa.getUsuario().getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O e-mail informado já está em uso.");
        }

        // 1. Cria e salva o Usuário
        Usuario novoUsuario = new Usuario();
        novoUsuario.setEmail(novaEmpresa.getUsuario().getEmail());
        novoUsuario.setSenha(passwordEncoder.encode(novaEmpresa.getUsuario().getSenha()));
        novoUsuario.setRole("ROLE_EMPRESA");
        Usuario usuarioSalvo = usuarioRepository.save(novoUsuario);

        novaEmpresa.setEmail(usuarioSalvo.getEmail());
        // 2. Associa o usuário salvo à empresa e salva a empresa
        novaEmpresa.setUsuario(usuarioSalvo);
        return empresaRepository.save(novaEmpresa);
    }

    @GetMapping("/me")
    @Operation(summary = "Busca o perfil da empresa logada", description = "Retorna os dados cadastrais da empresa que está autenticada.")
    public Empresa getMyProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
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
}