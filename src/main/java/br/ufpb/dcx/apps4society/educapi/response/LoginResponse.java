package br.ufpb.dcx.apps4society.educapi.response;

public class LoginResponse {

    private String token;

    //Remover o "Bearer " quando for refatorar, Bearer deve vir de outro local
    public LoginResponse(String token) {
        this.token = "Bearer " + token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
