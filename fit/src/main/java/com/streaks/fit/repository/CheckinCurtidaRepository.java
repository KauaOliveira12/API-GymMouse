package com.streaks.fit.repository;

import com.streaks.fit.model.CheckinCurtida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckinCurtidaRepository extends JpaRepository<CheckinCurtida, Long> {

    long countByCheckinId(Long checkinId);

    boolean existsByCheckinIdAndUsuarioId(Long checkinId, Long usuarioId);

    void deleteByCheckinIdAndUsuarioId(Long checkinId, Long usuarioId);
}
