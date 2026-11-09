package br.ufpb.dcx.apps4society.educapi.domain;

/**
 * Represents the access level of a User within the system.
 *
 * CLIENTE  - regular user, can manage only content they created.
 * ADMIN    - can moderate any Context/Challenge and delete CLIENTE accounts.
 * SYSADMIN - full control: can promote users to ADMIN and delete any account,
 *            including ADMIN accounts.
 */
public enum Role {
    CLIENTE,
    ADMIN,
    SYSADMIN
}
