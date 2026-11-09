package br.ufpb.dcx.apps4society.educapi.dto.log;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Result of a purge of old audit log entries: how many rows were removed,
 * the retention window (in days) that was applied, and the cutoff instant
 * used (entries strictly before this instant were deleted).
 */
public class LimpezaLogsResultDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private long removidos;
    private int diasRetencao;
    private LocalDateTime cortadoEm;

    public LimpezaLogsResultDTO() {
    }

    public LimpezaLogsResultDTO(long removidos, int diasRetencao, LocalDateTime cortadoEm) {
        this.removidos = removidos;
        this.diasRetencao = diasRetencao;
        this.cortadoEm = cortadoEm;
    }

    public long getRemovidos() {
        return removidos;
    }

    public int getDiasRetencao() {
        return diasRetencao;
    }

    public LocalDateTime getCortadoEm() {
        return cortadoEm;
    }
}
