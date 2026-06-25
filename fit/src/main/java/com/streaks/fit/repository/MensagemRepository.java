package com.streaks.fit.repository;

import com.streaks.fit.model.Mensagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository; // Adicione isso
import java.util.List; // O erro de "Cannot resolve symbol 'List'" vem da falta deste import

@Repository
public interface MensagemRepository extends JpaRepository<Mensagem, Long> {
    List<Mensagem> findByGrupoIdOrderByDataEnvioAsc(Long grupoId);
}