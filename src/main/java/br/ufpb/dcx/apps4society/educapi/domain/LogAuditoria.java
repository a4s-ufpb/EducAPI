package br.ufpb.dcx.apps4society.educapi.domain;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Represents a single entry in the system's audit trail: who did what,
 * to which entity, and when.
 *
 * The actor is stored as an email/name snapshot (not a foreign key to
 * {@link User}) on purpose: audit entries must remain intact and readable
 * even after the acting User account is later deleted from the system.
 */
@Entity
public class LogAuditoria implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String atorEmail;

    private String atorNome;

    // columnDefinition explícito: sem ele, o Hibernate 6 gera automaticamente
    // uma CHECK constraint no banco listando os valores do enum Java NO
    // MOMENTO em que a tabela é criada. Com ddl-auto=update, o Hibernate
    // nunca revisita/atualiza essa constraint depois — então toda vez que um
    // novo valor é adicionado a AcaoAuditoria (como LIMPEZA_LOGS), o INSERT
    // passa a violar a constraint antiga, mesmo com o enum já atualizado no
    // código. Foi exatamente isso que causou o erro 500 ao limpar os logs.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(30)")
    private AcaoAuditoria acao;

    @Column(nullable = false)
    private String tipoEntidade;

    private Long entidadeId;

    @Column(length = 500)
    private String detalhes;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    /**
     * Fixed UTC-3 offset (Brasilia time). The container's OS timezone is not
     * guaranteed to be America/Sao_Paulo (it defaults to UTC), so relying on
     * {@code LocalDateTime.now()} alone stores the wrong wall-clock time.
     * Same convention already used in {@code JWTService} for token expiry.
     */
    private static final ZoneOffset ZONA_BRASILIA = ZoneOffset.of("-03:00");

    public LogAuditoria() {
    }

    public LogAuditoria(String atorEmail, String atorNome, AcaoAuditoria acao, String tipoEntidade,
                         Long entidadeId, String detalhes) {
        this.atorEmail = atorEmail;
        this.atorNome = atorNome;
        this.acao = acao;
        this.tipoEntidade = tipoEntidade;
        this.entidadeId = entidadeId;
        this.detalhes = detalhes;
        this.timestamp = LocalDateTime.now(ZONA_BRASILIA);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAtorEmail() {
        return atorEmail;
    }

    public void setAtorEmail(String atorEmail) {
        this.atorEmail = atorEmail;
    }

    public String getAtorNome() {
        return atorNome;
    }

    public void setAtorNome(String atorNome) {
        this.atorNome = atorNome;
    }

    public AcaoAuditoria getAcao() {
        return acao;
    }

    public void setAcao(AcaoAuditoria acao) {
        this.acao = acao;
    }

    public String getTipoEntidade() {
        return tipoEntidade;
    }

    public void setTipoEntidade(String tipoEntidade) {
        this.tipoEntidade = tipoEntidade;
    }

    public Long getEntidadeId() {
        return entidadeId;
    }

    public void setEntidadeId(Long entidadeId) {
        this.entidadeId = entidadeId;
    }

    public String getDetalhes() {
        return detalhes;
    }

    public void setDetalhes(String detalhes) {
        this.detalhes = detalhes;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "LogAuditoria [id=" + id + ", atorEmail=" + atorEmail + ", acao=" + acao
                + ", tipoEntidade=" + tipoEntidade + ", entidadeId=" + entidadeId
                + ", timestamp=" + timestamp + "]";
    }
}
