package com.eliasgonzalez.cartones.zip;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ByteArrayResource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;

class ZipServiceTest {

    @DisplayName("crearZip - Happy Path con un solo archivo")
    @Test
    void testCrearZip_singleFile_happyPath() throws IOException {
        // Arrange
        Map<String, byte[]> archivos = new HashMap<>();
        String filename = "file1.txt";
        byte[] content = "Hello, World!".getBytes();
        archivos.put(filename, content);

        // Act
        Resource zipResource = ZipService.crearZip(archivos);

        // Assert
        assertThat(zipResource).isNotNull().isInstanceOf(ByteArrayResource.class);
        assertThat(zipResource.contentLength()).isGreaterThan(0);

        // Verify content of the zip file
        try (ZipInputStream zis = new ZipInputStream(zipResource.getInputStream())) {
            ZipEntry entry = zis.getNextEntry();
            assertThat(entry).isNotNull();
            assertThat(entry.getName()).isEqualTo(filename);
            byte[] extractedContent = zis.readAllBytes();
            assertThat(extractedContent).isEqualTo(content);
            assertThat(zis.getNextEntry()).isNull(); // Should only be one entry
        }
    }

    @DisplayName("crearZip - Happy Path con múltiples archivos")
    @Test
    void testCrearZip_multipleFiles_happyPath() throws IOException {
        // Arrange
        Map<String, byte[]> archivos = new HashMap<>();
        archivos.put("file1.txt", "Content of file 1".getBytes());
        archivos.put("file2.pdf", "Content of file 2".getBytes());
        archivos.put("image.jpg", "Content of image".getBytes());

        // Act
        Resource zipResource = ZipService.crearZip(archivos);

        // Assert
        assertThat(zipResource).isNotNull().isInstanceOf(ByteArrayResource.class);
        assertThat(zipResource.contentLength()).isGreaterThan(0);

        // Verify content of the zip file
        Map<String, byte[]> extractedFiles = new HashMap<>();
        try (ZipInputStream zis = new ZipInputStream(zipResource.getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                extractedFiles.put(entry.getName(), zis.readAllBytes());
            }
        }
        assertThat(extractedFiles).hasSize(3);
        assertThat(extractedFiles.get("file1.txt")).isEqualTo("Content of file 1".getBytes());
        assertThat(extractedFiles.get("file2.pdf")).isEqualTo("Content of file 2".getBytes());
        assertThat(extractedFiles.get("image.jpg")).isEqualTo("Content of image".getBytes());
    }

    @DisplayName("crearZip - Mapa de archivos vacío")
    @Test
    void testCrearZip_emptyMap() throws IOException {
        // Arrange
        Map<String, byte[]> archivos = new HashMap<>();

        // Act
        Resource zipResource = ZipService.crearZip(archivos);

        // Assert
        assertThat(zipResource).isNotNull().isInstanceOf(ByteArrayResource.class);
        assertThat(zipResource.contentLength()).isGreaterThan(0); // A valid empty zip file has some length

        try (ZipInputStream zis = new ZipInputStream(zipResource.getInputStream())) {
            assertThat(zis.getNextEntry()).isNull(); // No entries expected
        }
    }

    @DisplayName("crearZip - Archivos con contenido nulo o vacío deben ser omitidos")
    @Test
    void testCrearZip_nullOrEmptyContent() throws IOException {
        // Arrange
        Map<String, byte[]> archivos = new HashMap<>();
        archivos.put("valid.txt", "Valid content".getBytes());
        archivos.put("null_content.txt", null);
        archivos.put("empty_content.txt", new byte[0]);

        // Act
        Resource zipResource = ZipService.crearZip(archivos);

        // Assert
        assertThat(zipResource).isNotNull().isInstanceOf(ByteArrayResource.class);
        
        // Only valid.txt should be present
        Map<String, byte[]> extractedFiles = new HashMap<>();
        try (ZipInputStream zis = new ZipInputStream(zipResource.getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                extractedFiles.put(entry.getName(), zis.readAllBytes());
            }
        }
        assertThat(extractedFiles).hasSize(1);
        assertThat(extractedFiles).containsKey("valid.txt");
        assertThat(extractedFiles.get("valid.txt")).isEqualTo("Valid content".getBytes());
    }
}
