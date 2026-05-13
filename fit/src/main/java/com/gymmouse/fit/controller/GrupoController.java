package com.gymmouse.fit.controller;

import com.gymmouse.fit.model.Grupo;
import com.gymmouse.fit.repository.GrupoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/grupos")
public class GrupoController {

    @Autowired
    private GrupoRepository repository;

    @PostMapping
    public ResponseEntity<Grupo> criarGrupo(@RequestBody Grupo novoGrupo) {
        Grupo grupoSalvo = repository.save(novoGrupo);
        return ResponseEntity.ok(grupoSalvo);
    }

    @GetMapping
    public ResponseEntity<List<Grupo>> listarGrupos() {
        List<Grupo> grupos = repository.findAll();
        return ResponseEntity.ok(grupos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Grupo> buscarGrupoPorId(@PathVariable Long id) {
        Optional<Grupo> grupo = repository.findById(id);

        if (grupo.isPresent()) {
            return ResponseEntity.ok(grupo.get());
        }

        return ResponseEntity.status(404).build(); // Retorna erro 404 se o grupo não existir
    }
}
