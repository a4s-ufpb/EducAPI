package br.ufpb.dcx.apps4society.educapi.resources;

import java.util.List;
import javax.validation.Valid;

import br.ufpb.dcx.apps4society.educapi.dto.context.ContextRegisterDTO;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.ContextAlreadyExistsException;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.InvalidContextException;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.InvalidUserException;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.ufpb.dcx.apps4society.educapi.domain.Context;
import br.ufpb.dcx.apps4society.educapi.dto.context.ContextDTO;
import br.ufpb.dcx.apps4society.educapi.services.ContextService;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.ObjectNotFoundException;

@RestController
@RequestMapping(value = "/v1/api/")
@CrossOrigin("*")
public class ContextResource {

    @Autowired
    private ContextService contextService;

    @ApiOperation("Returns a Context, if the Context ID are valid.")
    @GetMapping("contexts/{idContext}")
    public ResponseEntity<Context> find(@PathVariable Long idContext) {
        return ResponseEntity.ok(contextService.find(idContext));
    }

    @ApiOperation("Adds a new Context to the service, if the token is valid.")
    @PostMapping("auth/contexts")
    public ResponseEntity<ContextDTO> insert(@RequestHeader("Authorization") String token,
                                             @Valid @RequestBody ContextRegisterDTO objDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contextService.insert(token, objDto));
    }

    @ApiOperation("Updates a User Context, if the token and the Context ID are valid.")
    @PutMapping("auth/contexts/{idContext}")
    public ResponseEntity<ContextDTO> update(@RequestHeader("Authorization") String token,
                                             @Valid @RequestBody ContextRegisterDTO objDto,
                                             @PathVariable Long idContext) {
       return ResponseEntity.ok(contextService.update(token,objDto,idContext));
    }

    @ApiOperation("Deletes a User Context from the service, if the token and the Context ID are valid.")
    @DeleteMapping("auth/contexts/{idContext}")
    public ResponseEntity<ContextDTO> delete(@RequestHeader("Authorization") String token,
                                             @PathVariable Long idContext) {
        return ResponseEntity.ok(contextService.delete(token,idContext));
    }

    @ApiOperation("Returns a list of Contexts registered in the service.")
    @GetMapping("contexts")
    public ResponseEntity<Page<Context>> findContextsByParams(
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "size", defaultValue = "20") Integer size,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            Pageable pageable){
        return new ResponseEntity<>(contextService.findContextsByParams(email, name, pageable), HttpStatus.OK);

    }


    @ApiOperation("Returns a list of all Contexts registered by the request User, if the token is valid.")
    @GetMapping("auth/contexts")
    public ResponseEntity<List<ContextDTO>> findAllByUser(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(contextService.findContextsByCreator(token));
    }

}