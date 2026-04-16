package br.ufpb.dcx.apps4society.educapi.resources;


import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import br.ufpb.dcx.apps4society.educapi.services.MinioService;
import br.ufpb.dcx.apps4society.educapi.services.UserService;
import io.swagger.v3.oas.annotations.Operation;


@RestController
@RequestMapping(value = "/v1/api/")
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
            if (contentType == null
                    || (!contentType.equals("image/png")
                    && !contentType.equals("image/jpeg"))) {


                return ResponseEntity.badRequest().body("Apenas imagens PNG ou JPEG são permitidas");
            }

            String originalName = file.getOriginalFilename();
            if (originalName == null) {
                originalName = "file";
            }


            InputStream inputStream = file.getInputStream();

            String result = minioService.uploadFile(
                    originalName,
                    inputStream,
                    file.getSize(),
                    contentType
            );


            Map<String, String> response = new HashMap<>();
            response.put("url", result);


            return ResponseEntity.ok(response);


        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
