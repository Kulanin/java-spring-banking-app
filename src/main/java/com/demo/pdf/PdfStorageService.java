package com.demo.pdf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PdfStorageService {

    @Value("${pdf.storage.path:/tmp/pdfs}")
    private String storagePath;

    public String savePdf(String fileName, byte[] content) throws IOException {

        Path path = Paths.get(storagePath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }

        Path filePath = path.resolve(fileName);

        Files.write(filePath, content);

        return fileName;
    }

}
