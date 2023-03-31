package br.ufpb.dcx.apps4society.educapi.unit;

import org.springframework.core.io.ClassPathResource;

import java.nio.file.Files;

public class FileUtils {

    public static String getJsonFromFile(String fileName) throws Exception{
        ClassPathResource resource = new ClassPathResource(fileName);
        return new String(Files.readAllBytes(resource.getFile().toPath()));

    }
}
