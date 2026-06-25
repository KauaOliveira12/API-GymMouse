package com.streaks.fit.repository;

import com.streaks.fit.model.RegistroPontuacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegistroPontuacaoRepository extends JpaRepository<RegistroPontuacao, Long> {
}
