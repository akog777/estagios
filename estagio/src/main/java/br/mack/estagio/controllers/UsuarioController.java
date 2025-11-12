package br.mack.estagio.controllers;

import br.mack.estagio.entities.Usuario;
import br.mack.estagio.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Usuario criar(@RequestBody Usuario novoUsuario) {
        if (novoUsuario.getEmail() == null || novoUsuario.getEmail().isEmpty() ||
            novoUsuario.getSenha() == null || novoUsuario.getSenha().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email e Senha são obrigatórios");
        }
        // REGRA: Criptografando a senha antes de salvar
        String senhaCriptografada = passwordEncoder.encode(novoUsuario.getSenha());
        novoUsuario.setSenha(senhaCriptografada);
        return repository.save(novoUsuario);
    }

    @PostMapping("/login")
    public Usuario login(@RequestBody Usuario dadosLogin) {
        // 1. Busca o usuário pelo login.
        Usuario usuario = repository.findByEmail(dadosLogin.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas"));

        // 2. Usa o passwordEncoder para verificar se a senha enviada corresponde à senha criptografada no banco.
        if (passwordEncoder.matches(dadosLogin.getSenha(), usuario.getSenha())) {
            // 3. Se a senha corresponder, o login é bem-sucedido.
            // Em uma aplicação real, aqui você geraria um Token (JWT).
            // Por enquanto, vamos retornar o objeto do usuário sem a senha.
            usuario.setSenha(null); // Nunca retorne a senha na resposta!
            return usuario;
        } else {
            // 4. Se a senha não corresponder, lança um erro.
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
        }
    }


    @GetMapping
    public List<Usuario> lerTudo() {
        return (List<Usuario>) repository.findAll();
    }

    @GetMapping("/{id}")
    public Usuario lerPorId(@PathVariable Long id) {
        Optional<Usuario> optional = repository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado");
    }

    @PutMapping("/{id}")
    public Usuario atualizar(@PathVariable Long id, @RequestBody Usuario usuarioAtualizado) {
        Optional<Usuario> optional = repository.findById(id);
        if (optional.isPresent()) {
            Usuario usuario = optional.get();
            usuario.setEmail(usuarioAtualizado.getEmail());
            // Criptografa a senha apenas se ela foi alterada
            if (usuarioAtualizado.getSenha() != null && !usuarioAtualizado.getSenha().isEmpty()) {
                String senhaCriptografada = passwordEncoder.encode(usuarioAtualizado.getSenha());
                usuario.setSenha(senhaCriptografada);
            }
            return repository.save(usuario);
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado");
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void apagar(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado");
        }
        repository.deleteById(id);
    }
}