package com.streaks.fit.repository;

import com.streaks.fit.model.Grupo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GrupoRepository extends JpaRepository<Grupo, Long> {

    Optional<Grupo> findByCodigoAcesso(String codigoAcesso);

    boolean existsByCodigoAcesso(String codigoAcesso);
}