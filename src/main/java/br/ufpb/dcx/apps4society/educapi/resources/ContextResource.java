package br.ufpb.dcx.apps4society.educapi.resources;

import br.ufpb.dcx.apps4society.educapi.domain.Context;
import br.ufpb.dcx.apps4society.educapi.dto.context.ContextDTO;
import br.ufpb.dcx.apps4society.educapi.dto.context.ContextRegisterDTO;
import br.ufpb.dcx.apps4society.educapi.services.ContextService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.beans.PropertyEditorSupport;
import java.io.IOException;

import java.util.List;

@RestController
@RequestMapping(value = "/v1/api/")
@CrossOrigin("*")
public class ContextResource {

    @Autowired
    private ContextService contextService;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(MultipartFile.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                if (text == null || text.isBlank()) {
                    setValue(null);
                }
            }
        });
    }

    @Operation(summary = "Returns a Context, if the Context ID are valid.")
    @GetMapping("contexts/{idContext}")
    public ResponseEntity<Context> find(@PathVariable Long idContext) {
        return ResponseEntity.ok(contextService.find(idContext));
    }

    @Operation(summary = "Returns the Context image bytes, using the MinIO image first and imageBackup as fallback.")
    @GetMapping("contexts/{idContext}/image")
    public ResponseEntity<byte[]> getImage(@PathVariable Long idContext) {
        byte[] imageBytes = contextService.getContextImage(idContext);

        return ResponseEntity.ok()
                .contentType(resolveImageContentType(imageBytes))
                .body(imageBytes);
    }

    @Operation(summary = "Adds a new Context via file upload, if the token is valid.")
    @PostMapping(
            value = "auth/contexts",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<ContextDTO> insertWithFile(
            @RequestHeader("Authorization") String token,
            @Valid @ModelAttribute ContextRegisterDTO objDto
    ) throws IOException {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(contextService.insert(token, objDto));
    }

    @Operation(summary = "Adds a new Context via JSON (imageUrl), if the token is valid.")
    @PostMapping(
            value = "auth/contexts",
            consumes = "application/json"
    )
    public ResponseEntity<ContextDTO> insertWithJson(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody ContextRegisterDTO objDto
    ) throws IOException {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(contextService.insert(token, objDto));
    }

    @Operation(summary = "Updates a User Context via file upload, if the token and the Context ID are valid.")
    @PutMapping(
            value = "auth/contexts/{idContext}",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<ContextDTO> updateWithFile(
            @RequestHeader("Authorization") String token,
            @Valid @ModelAttribute ContextRegisterDTO objDto,
            @PathVariable Long idContext
    ) throws IOException {
        return ResponseEntity.ok(
                contextService.update(token, objDto, idContext)
        );
    }

    @Operation(summary = "Updates a User Context via JSON (imageUrl), if the token and the Context ID are valid.")
    @PutMapping(
            value = "auth/contexts/{idContext}",
            consumes = "application/json"
    )
    public ResponseEntity<ContextDTO> updateWithJson(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody ContextRegisterDTO objDto,
            @PathVariable Long idContext
    ) throws IOException {
        return ResponseEntity.ok(
                contextService.update(token, objDto, idContext)
        );
    }

    @Operation(summary = "Deletes a User Context from the service, if the token and the Context ID are valid.")
    @DeleteMapping("auth/contexts/{idContext}")
    public ResponseEntity<ContextDTO> delete(@RequestHeader("Authorization") String token,
            @PathVariable Long idContext
    ) {
        return ResponseEntity.ok(contextService.delete(token, idContext));
    }

    @Operation(summary = "Returns a list of Contexts registered in the service.")
    @GetMapping("contexts")
    public ResponseEntity<Page<Context>> findContextsByParams(
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "size", defaultValue = "20") Integer size,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            Pageable pageable
    ) {
        return new ResponseEntity<>(contextService.findContextsByParams(email, name, pageable), HttpStatus.OK);

    }

    @Operation(summary = "Returns a list of all Contexts registered by the request User, if the token is valid.")
    @GetMapping("auth/contexts")
    public ResponseEntity<List<ContextDTO>> findAllByUser(@RequestHeader("Authorization") String token
    ) {
        return ResponseEntity.ok(contextService.findContextsByCreator(token));
    }

    private MediaType resolveImageContentType(byte[] imageBytes) {
        if (imageBytes.length >= 8
                && imageBytes[0] == (byte) 0x89
                && imageBytes[1] == 0x50
                && imageBytes[2] == 0x4E
                && imageBytes[3] == 0x47) {
            return MediaType.IMAGE_PNG;
        }

        return MediaType.IMAGE_JPEG;
    }

}
