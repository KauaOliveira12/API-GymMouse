package com.streaks.fit.repository;

import com.streaks.fit.model.PreferenciaNotificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PreferenciaNotificacaoRepository extends JpaRepository<PreferenciaNotificacao, Long> {

    Optional<PreferenciaNotificacao> findByUsuarioId(Long usuarioId);

    @Query("SELECT p FROM PreferenciaNotificacao p JOIN FETCH p.usuario WHERE p.usuario.id IN :usuarioIds")
    List<PreferenciaNotificacao> findByUsuarioIdIn(@Param("usuarioIds") Collection<Long> usuarioIds);
}
