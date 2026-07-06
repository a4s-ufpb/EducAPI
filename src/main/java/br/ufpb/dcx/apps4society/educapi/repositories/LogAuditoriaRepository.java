package br.ufpb.dcx.apps4society.educapi.repositories;

import br.ufpb.dcx.apps4society.educapi.domain.AcaoAuditoria;
import br.ufpb.dcx.apps4society.educapi.domain.LogAuditoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface LogAuditoriaRepository extends JpaRepository<LogAuditoria, Long> {

    Page<LogAuditoria> findAllByAtorEmailContainingIgnoreCase(String atorEmail, Pageable pageable);

    Page<LogAuditoria> findAllByAcao(AcaoAuditoria acao, Pageable pageable);

    Page<LogAuditoria> findAllByAtorEmailContainingIgnoreCaseAndAcao(String atorEmail, AcaoAuditoria acao, Pageable pageable);

    /**
     * Bulk-deletes every entry older than {@code cortadoEm} (exclusive of
     * newer entries). Returns the number of rows removed, so the caller can
     * report it back and log it in the audit trail.
     */
    long deleteByTimestampBefore(LocalDateTime cortadoEm);
}
