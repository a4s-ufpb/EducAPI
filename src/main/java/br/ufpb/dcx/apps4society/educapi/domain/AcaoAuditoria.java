package br.ufpb.dcx.apps4society.educapi.domain;

/**
 * Represents an auditable action performed within the system, recorded by
 * {@code LogAuditoria}.
 */
public enum AcaoAuditoria {
    LOGIN,
    CRIACAO_TEMA,
    EXCLUSAO_TEMA,
    CRIACAO_DESAFIO,
    EXCLUSAO_DESAFIO,
    EXCLUSAO_USUARIO,
    PROMOCAO_ADMIN,
    DEMOCAO_ADMIN,
    LIMPEZA_LOGS
}
