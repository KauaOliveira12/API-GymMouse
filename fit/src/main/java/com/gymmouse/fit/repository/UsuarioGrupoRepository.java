package com.gymmouse.fit.repository;

import com.gymmouse.fit.model.Grupo;
import com.gymmouse.fit.model.UsuarioGrupo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioGrupoRepository extends JpaRepository<UsuarioGrupo, Long> {

    boolean existsByUsuarioIdAndGrupoId(Long usuarioId, Long grupoId);

    Optional<UsuarioGrupo> findByUsuarioIdAndGrupoId(Long usuarioId, Long grupoId);

    void deleteByUsuarioIdAndGrupoId(Long usuarioId, Long grupoId);

    void deleteByGrupoId(Long grupoId);

    @Query("SELECT ug.grupo FROM UsuarioGrupo ug JOIN ug.grupo g WHERE ug.usuario.id = :usuarioId ORDER BY g.nome ASC")
    List<Grupo> findGruposByUsuarioId(@Param("usuarioId") Long usuarioId);

    /**
     * Pontos somados desde o início do dia de entrada no grupo (MySQL DATE).
     */
    @Query(
            value = """
                    SELECT u.id, u.nome, COALESCE(SUM(r.quantidade), 0) AS pts
                    FROM usuario_grupo ug
                    INNER JOIN usuarios u ON u.id = ug.usuario_id
                    LEFT JOIN registro_pontuacao r ON r.usuario_id = u.id
                        AND r.data_registro >= DATE(ug.data_entrada)
                    WHERE ug.grupo_id = :grupoId
                    GROUP BY u.id, u.nome
                    ORDER BY pts DESC
                    """,
            nativeQuery = true
    )
    List<Object[]> findRankingNativo(@Param("grupoId") Long grupoId);
}
