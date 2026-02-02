package com.eliasgonzalez.cartones.zip;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class LimpiezaStorageJobTest {

    private LimpiezaStorageJob limpiezaStorageJob;

    @TempDir
    Path tempDir; // JUnit 5 provides a temporary directory for each test

    private Path storagePath;

    @BeforeEach
    void setUp() throws IOException {
        storagePath = tempDir.resolve("storage");
        Files.createDirectory(storagePath);
        limpiezaStorageJob = new LimpiezaStorageJob(storagePath.toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        // Ensure the temporary storage directory and its contents are deleted after each test
        if (Files.exists(storagePath)) {
            Files.walk(storagePath)
                    .sorted(java.util.Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            // Log or handle the exception appropriately, but allow test to continue cleanup
                            System.err.println("Error al limpiar " + path + ": " + e.getMessage());
                        }
                    });
        }
    }

    @DisplayName("ejecutarLimpiezaTrimestral - Happy Path: Elimina contenido del directorio storage")
    @Test
    void testEjecutarLimpiezaTrimestral_happyPath() throws IOException {
        // Arrange
        // Create dummy files and subdirectories inside storagePath
        Path subDir1 = Files.createDirectory(storagePath.resolve("subdir1"));
        Files.createFile(subDir1.resolve("file1.txt"));
        Files.createFile(storagePath.resolve("file2.pdf"));
        Path subDir2 = Files.createDirectory(storagePath.resolve("subdir2"));
        Files.createFile(subDir2.resolve("nestedFile.txt"));

        // Assert initial state
        assertThat(Files.list(storagePath)).hasSize(3); // subdir1, file2.pdf, subdir2

        // Act
        limpiezaStorageJob.ejecutarLimpiezaTrimestral();

        // Assert final state
        assertThat(Files.list(storagePath)).isEmpty(); // All contents should be deleted
        assertThat(Files.exists(storagePath)).isTrue(); // The 'storage' directory itself should remain
    }

    @DisplayName("ejecutarLimpiezaTrimestral - Directorio storage no existe")
    @Test
    void testEjecutarLimpiezaTrimestral_storageDirDoesNotExist() throws IOException {
        // Arrange
        // Delete the storagePath created in setUp
        Files.walk(storagePath)
                .sorted(java.util.Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        System.err.println("Error during setup cleanup: " + e.getMessage());
                    }
                });
        assertThat(Files.exists(storagePath)).isFalse();

        // Act
        limpiezaStorageJob.ejecutarLimpiezaTrimestral();

        // Assert (no exception, no directory created, no side effects)
        assertThat(Files.exists(storagePath)).isFalse();
    }
}