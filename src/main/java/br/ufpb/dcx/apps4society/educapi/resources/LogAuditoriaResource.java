package br.ufpb.dcx.apps4society.educapi.resources;

import br.ufpb.dcx.apps4society.educapi.domain.AcaoAuditoria;
import br.ufpb.dcx.apps4society.educapi.domain.User;
import br.ufpb.dcx.apps4society.educapi.dto.log.LimpezaLogsResultDTO;
import br.ufpb.dcx.apps4society.educapi.dto.log.LogAuditoriaDTO;
import br.ufpb.dcx.apps4society.educapi.services.LogAuditoriaService;
import br.ufpb.dcx.apps4society.educapi.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Read-only access to the system's audit trail, plus the SYSADMIN-only
 * retention purge. Restricted via {@code @PreAuthorize}, enforced using the
 * role-based authorities populated by {@code JwtAuthenticationFilter}.
 * ADMIN's scope is limited to theme/challenge moderation and does not
 * include the audit log.
 */
@RestController
@RequestMapping(value = "/v1/api/")
@CrossOrigin("*")
public class LogAuditoriaResource {

    @Autowired
    private LogAuditoriaService logAuditoriaService;

    @Autowired
    private UserService userService;

    @Operation(summary = "Returns a paginated, optionally filtered list of audit log entries, most recent first. Restricted to SYSADMIN.")
    @PreAuthorize("hasRole('SYSADMIN')")
    @GetMapping("admin/logs")
    public ResponseEntity<Page<LogAuditoriaDTO>> findLogs(
            @RequestParam(value = "atorEmail", required = false) String atorEmail,
            @RequestParam(value = "acao", required = false) AcaoAuditoria acao,
            @RequestParam(value = "size", defaultValue = "20") Integer size,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @PageableDefault(size = 20, sort = "timestamp", direction = org.springframework.data.domain.Sort.Direction.DESC)
            Pageable pageable) {
        Page<LogAuditoriaDTO> logs = logAuditoriaService.buscar(atorEmail, acao, pageable)
                .map(LogAuditoriaDTO::new);
        return new ResponseEntity<>(logs, HttpStatus.OK);
    }

    @Operation(summary = "Purges audit log entries older than the given retention window (in days). "
            + "Restricted to SYSADMIN. Minimum retention is 30 days.")
    @PreAuthorize("hasRole('SYSADMIN')")
    @DeleteMapping("admin/logs")
    public ResponseEntity<LimpezaLogsResultDTO> limparLogsAntigos(
            @RequestHeader("Authorization") String token,
            @RequestParam(value = "diasRetencao") Integer diasRetencao) {
        User ator = userService.find(token);
        LogAuditoriaService.LogPurgeResult resultado = logAuditoriaService.limparAntigos(ator, diasRetencao);
        return ResponseEntity.ok(
                new LimpezaLogsResultDTO(resultado.removidos(), diasRetencao, resultado.cortadoEm()));
    }
}
