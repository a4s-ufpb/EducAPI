package br.ufpb.dcx.apps4society.educapi.services;

import br.ufpb.dcx.apps4society.educapi.domain.AcaoAuditoria;
import br.ufpb.dcx.apps4society.educapi.domain.LogAuditoria;
import br.ufpb.dcx.apps4society.educapi.domain.User;
import br.ufpb.dcx.apps4society.educapi.repositories.LogAuditoriaRepository;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.InvalidRetentionPeriodException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class LogAuditoriaService {

    private static final Logger logger = LoggerFactory.getLogger(LogAuditoriaService.class);

    /**
     * Piso de segurança: nao aceitamos um pedido de limpeza com retencao
     * menor que isso, para evitar apagar o rastro de auditoria por engano
     * (ex.: um "0" ou "1" digitado sem querer no campo de dias).
     */
    private static final int RETENCAO_MINIMA_DIAS = 30;

    private static final ZoneOffset ZONA_BRASILIA = ZoneOffset.of("-03:00");

    @Autowired
    private LogAuditoriaRepository logAuditoriaRepository;

    public LogAuditoriaService(LogAuditoriaRepository logAuditoriaRepository) {
        this.logAuditoriaRepository = logAuditoriaRepository;
    }

    /**
     * Records an audit entry. Never throws: a failure to persist the audit
     * trail must not roll back or block the business action that triggered
     * it, so any error is logged and swallowed here.
     *
     * @param ator         the User who performed the action (may be null for
     *                     system-initiated actions, e.g. the SYSADMIN seeder).
     * @param acao         the action performed.
     * @param tipoEntidade the type of the affected entity (e.g. "Context", "Challenge", "User").
     * @param entidadeId   the id of the affected entity, if applicable.
     * @param detalhes     a short human-readable description (e.g. the name/word affected).
     */
    public void registrar(User ator, AcaoAuditoria acao, String tipoEntidade, Long entidadeId, String detalhes) {
        try {
            String email = ator != null ? ator.getEmail() : "sistema";
            String nome = ator != null ? ator.getName() : "Sistema";

            LogAuditoria log = new LogAuditoria(email, nome, acao, tipoEntidade, entidadeId, detalhes);
            logAuditoriaRepository.save(log);
        } catch (RuntimeException e) {
            logger.warn("Nao foi possivel registrar log de auditoria. acao={}, tipoEntidade={}, entidadeId={}",
                    acao, tipoEntidade, entidadeId, e);
        }
    }

    public Page<LogAuditoria> buscar(String atorEmail, AcaoAuditoria acao, Pageable pageable) {
        boolean hasEmail = atorEmail != null && !atorEmail.isBlank();

        if (hasEmail && acao != null) {
            return logAuditoriaRepository.findAllByAtorEmailContainingIgnoreCaseAndAcao(atorEmail, acao, pageable);
        } else if (hasEmail) {
            return logAuditoriaRepository.findAllByAtorEmailContainingIgnoreCase(atorEmail, pageable);
        } else if (acao != null) {
            return logAuditoriaRepository.findAllByAcao(acao, pageable);
        }

        return logAuditoriaRepository.findAll(pageable);
    }

    /**
     * Purges every audit log entry older than {@code diasRetencao} days.
     * Restricted to SYSADMIN at the controller level; the caller ({@code ator})
     * is only used here to record who triggered the purge.
     *
     * @param ator          the SYSADMIN who requested the purge.
     * @param diasRetencao  how many days of history to keep (entries older
     *                      than this are deleted). Must be at least
     *                      {@value #RETENCAO_MINIMA_DIAS}.
     * @return how many rows were removed, and the cutoff instant used.
     */
    @Transactional
    public LogPurgeResult limparAntigos(User ator, int diasRetencao) {
        if (diasRetencao < RETENCAO_MINIMA_DIAS) {
            throw new InvalidRetentionPeriodException(
                    "Periodo de retencao minimo e de " + RETENCAO_MINIMA_DIAS + " dias. Valor informado: "
                    + diasRetencao + ".");
        }

        LocalDateTime cortadoEm = LocalDateTime.now(ZONA_BRASILIA).minusDays(diasRetencao);
        long removidos = logAuditoriaRepository.deleteByTimestampBefore(cortadoEm);

        registrar(ator, AcaoAuditoria.LIMPEZA_LOGS, "LogAuditoria", null,
                "Removidos " + removidos + " registro(s) anteriores a " + cortadoEm
                + " (retencao=" + diasRetencao + " dias)");

        return new LogPurgeResult(removidos, cortadoEm);
    }

    /**
     * Small holder for the result of {@link #limparAntigos}, avoiding a
     * dependency from this service package onto the resource-layer DTO.
     */
    public record LogPurgeResult(long removidos, LocalDateTime cortadoEm) {
    }
}
