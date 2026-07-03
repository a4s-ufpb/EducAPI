package br.ufpb.dcx.apps4society.educapi.dto.user;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotEmpty;
import java.io.Serializable;

public class UserChangePasswordDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "Required")
    private String currentPassword;

    @NotEmpty(message = "Required")
    @Length(min = 8, max = 12, message = "The size must be between 8 and 12 characters")
    private String newPassword;

    public UserChangePasswordDTO() {
    }

    public UserChangePasswordDTO(String currentPassword, String newPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
