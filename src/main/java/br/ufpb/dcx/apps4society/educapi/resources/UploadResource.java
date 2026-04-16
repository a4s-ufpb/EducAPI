package br.ufpb.dcx.apps4society.educapi.resources;


import br.ufpb.dcx.apps4society.educapi.services.MinioService;
import br.ufpb.dcx.apps4society.educapi.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.InputStream;
import java.util.UUID;


@RestController
@RequestMapping(value="/v1/api/")
@CrossOrigin("*")
public class UploadResource {


    private final MinioService minioService;
    private final UserService userService;


    public UploadResource(MinioService minioService, UserService userService) {
        this.minioService = minioService;
        this.userService = userService;
    }


    @Operation(summary = "Upload de imagem (requer token válido).")
    @PostMapping(value = "auth/upload", consumes = "multipart/form-data")
    public ResponseEntity<?> upload(
            @RequestHeader("Authorization") String token,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            // valida o token
            userService.find(token);


            // verifica se o arquivo é vazio
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Arquivo vazio");
            }


            // limita o tamanho (5MB)
            long maxSize = 5 * 1024 * 1024;
            if (file.getSize() > maxSize) {
                return ResponseEntity.badRequest().body("Arquivo muito grande (máx 5MB)");
            }


            // valida o tipo
            String contentType = file.getContentType();
            if (contentType == null ||
                (!contentType.equals("image/png") &&
                 !contentType.equals("image/jpeg"))) {


                return ResponseEntity.badRequest().body("Apenas imagens PNG ou JPEG são permitidas");
            }


            // 🔒 nome seguro
            String originalName = file.getOriginalFilename();
            String safeFileName = UUID.randomUUID() + "_" + originalName;


            InputStream inputStream = file.getInputStream();


            String result = minioService.uploadFile(
                    safeFileName,
                    inputStream,
                    file.getSize(),
                    contentType
            );


            return ResponseEntity.ok(result);


        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro no upload");
        }
    }
}
