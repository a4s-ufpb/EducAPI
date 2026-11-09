package br.ufpb.dcx.apps4society.educapi.dto.log;

import br.ufpb.dcx.apps4society.educapi.domain.AcaoAuditoria;
import br.ufpb.dcx.apps4society.educapi.domain.LogAuditoria;

import java.io.Serializable;
import java.time.LocalDateTime;

public class LogAuditoriaDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String atorEmail;
    private String atorNome;
    private AcaoAuditoria acao;
    private String tipoEntidade;
    private Long entidadeId;
    private String detalhes;
    private LocalDateTime timestamp;

    public LogAuditoriaDTO() {
    }

    public LogAuditoriaDTO(LogAuditoria log) {
        this.id = log.getId();
        this.atorEmail = log.getAtorEmail();
        this.atorNome = log.getAtorNome();
        this.acao = log.getAcao();
        this.tipoEntidade = log.getTipoEntidade();
        this.entidadeId = log.getEntidadeId();
        this.detalhes = log.getDetalhes();
        this.timestamp = log.getTimestamp();
    }

    public Long getId() {
        return id;
    }

    public String getAtorEmail() {
        return atorEmail;
    }

    public String getAtorNome() {
        return atorNome;
    }

    public AcaoAuditoria getAcao() {
        return acao;
    }

    public String getTipoEntidade() {
        return tipoEntidade;
    }

    public Long getEntidadeId() {
        return entidadeId;
    }

    public String getDetalhes() {
        return detalhes;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
