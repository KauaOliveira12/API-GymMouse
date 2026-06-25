package com.streaks.fit.repository;

import com.streaks.fit.model.Checkin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CheckinRepository extends JpaRepository<Checkin, Long> {

    List<Checkin> findByGrupoIdOrderByDataCriacaoDesc(Long grupoId);

    List<Checkin> findByUsuarioIdAndGrupoId(Long usuarioId, Long grupoId);
}
