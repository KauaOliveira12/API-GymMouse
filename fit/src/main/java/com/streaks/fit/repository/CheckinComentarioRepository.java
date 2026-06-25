package com.streaks.fit.repository;

import com.streaks.fit.model.CheckinComentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CheckinComentarioRepository extends JpaRepository<CheckinComentario, Long> {

    long countByCheckinId(Long checkinId);

    List<CheckinComentario> findByCheckinIdOrderByDataCriacaoAsc(Long checkinId);
}
