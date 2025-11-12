package br.mack.estagio.controllers;

import br.mack.estagio.entities.Usuario;
import br.mack.estagio.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository repository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Usuario criar(@RequestBody Usuario novoUsuario) {
        if (novoUsuario.getLogin() == null || novoUsuario.getLogin().isEmpty() ||
            novoUsuario.getSenha() == null || novoUsuario.getSenha().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Login e Senha são obrigatórios");
        }
        // Aqui seria o local ideal para criptografar a senha antes de salvar
        return repository.save(novoUsuario);
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
            usuario.setLogin(usuarioAtualizado.getLogin());
            usuario.setSenha(usuarioAtualizado.getSenha()); // Criptografar aqui também
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