package com.eliasgonzalez.cartones.zip;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipService {

    public static Path crearZip(List<Path> paths, String procesoId) throws IOException {
        Path directoryPath = Paths.get("storage", procesoId);
        // 1. Asegurar que las carpetas existan
        Files.createDirectories(directoryPath);

        Path zipPath = directoryPath.resolve("pdfs.zip");

        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            for (Path path : paths){
                if (Files.exists(path)) { // Validación de seguridad
                    agregarPdf(zos, path);
                }
            }
        }
        return zipPath;
    }

    private static void agregarPdf(ZipOutputStream zos, Path pdf) throws IOException {
        zos.putNextEntry(new ZipEntry(pdf.getFileName().toString()));
        Files.copy(pdf, zos);
        zos.closeEntry();
    }
}