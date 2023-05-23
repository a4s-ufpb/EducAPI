package br.ufpb.dcx.apps4society.educapi.utils;

import org.springframework.core.io.ClassPathResource;

import java.nio.file.Files;

public class FileUtils {

    public static String getJsonFromFile(String fileLocation) throws Exception{
        ClassPathResource resource = new ClassPathResource(fileLocation);
        return new String(Files.readAllBytes(resource.getFile().toPath()));

    }
}
