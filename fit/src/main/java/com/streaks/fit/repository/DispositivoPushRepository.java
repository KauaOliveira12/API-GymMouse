package com.streaks.fit.repository;

import com.streaks.fit.model.DispositivoPush;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface DispositivoPushRepository extends JpaRepository<DispositivoPush, Long> {

    Optional<DispositivoPush> findByToken(String token);

    List<DispositivoPush> findByUsuarioIdIn(Collection<Long> usuarioIds);

    void deleteByToken(String token);

    void deleteByUsuarioIdAndToken(Long usuarioId, String token);
}
