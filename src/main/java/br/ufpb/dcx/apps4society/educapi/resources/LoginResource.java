package br.ufpb.dcx.apps4society.educapi.resources;

import br.ufpb.dcx.apps4society.educapi.dto.user.UserLoginDTO;
import br.ufpb.dcx.apps4society.educapi.response.LoginResponse;
import br.ufpb.dcx.apps4society.educapi.services.JWTService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/v1/api/")
@CrossOrigin("*")
public class LoginResource {

    @Autowired
    private JWTService jwtService;

    @Operation(summary = "Returns a user authentication token.")
    @PostMapping("auth/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody UserLoginDTO userLoginDTO){
        return ResponseEntity.ok(jwtService.authenticate(userLoginDTO));
    }

}
