package com.eliasgonzalez.cartones.zip;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.stream.Stream;

@Component
public class LimpiezaStorageJob {

    @Scheduled(cron = "0 0 0 1 1/3 ?")
    public void ejecutarLimpiezaTrimestral() {
        Path storageDir = Paths.get("storage");

        if (!Files.exists(storageDir)) return;

        try (Stream<Path> s = Files.walk(storageDir)) {
            s.sorted(Comparator.reverseOrder()) // Borra archivos antes que carpetas
                    .forEach(path -> {
                        try {
                            // Evita borrar la carpeta raíz 'storage'
                            if (!path.equals(storageDir)) {
                                Files.delete(path);
                            }
                        } catch (IOException e) {
                            System.err.println("No se pudo borrar: " + path);
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
