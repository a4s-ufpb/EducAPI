package br.ufpb.dcx.apps4society.educapi.resources;

import br.ufpb.dcx.apps4society.educapi.dto.user.UserLoginDTO;
import br.ufpb.dcx.apps4society.educapi.response.LoginResponse;
import br.ufpb.dcx.apps4society.educapi.services.JWTService;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.InvalidUserException;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/v1/api/")
public class LoginResource {

    @Autowired
    private JWTService jwtService;

    @ApiOperation("Returns a user authentication token.")
    @PostMapping("auth/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody UserLoginDTO userLoginDTO){
        try{
            return new ResponseEntity<>(jwtService.authenticate(userLoginDTO), HttpStatus.OK);
        }catch (InvalidUserException exception){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }
}
