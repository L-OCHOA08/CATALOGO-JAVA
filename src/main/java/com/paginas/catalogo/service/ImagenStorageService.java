package com.paginas.catalogo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImagenStorageService {

    private final Path carpeta;

    public ImagenStorageService(@Value("${app.uploads.dir:uploads}") String dir) throws IOException {
        this.carpeta = Paths.get(dir).toAbsolutePath().normalize();
        Files.createDirectories(carpeta);
    }

    public Path getCarpeta() {
        return carpeta;
    }

    /** Devuelve el nombre guardado, o null si no se envió archivo. */
    public String guardar(MultipartFile archivo) throws IOException {
        if (archivo == null || archivo.isEmpty()) {
            return null;
        }
        String ext = detectarExtension(archivo);
        if (ext == null) {
            throw new IllegalArgumentException("Formato no permitido. Usá JPG, PNG o WEBP.");
        }
        String nombre = UUID.randomUUID() + "." + ext;
        try (InputStream in = archivo.getInputStream()) {
            Files.copy(in, carpeta.resolve(nombre));
        }
        return nombre;
    }

    public void borrar(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return;
        }
        try {
            Path destino = carpeta.resolve(nombre).normalize();
            if (destino.startsWith(carpeta)) {
                Files.deleteIfExists(destino);
            }
        } catch (IOException e) {
            // si no se puede borrar, no frenamos la operación
        }
    }

    // Mira los primeros bytes del archivo en vez de confiar en el nombre o en el tipo que declara el navegador
    private String detectarExtension(MultipartFile archivo) throws IOException {
        byte[] h = new byte[12];
        int n;
        try (InputStream in = archivo.getInputStream()) {
            n = in.readNBytes(h, 0, 12);
        }
        if (n < 12) return null;
        if ((h[0] & 0xFF) == 0xFF && (h[1] & 0xFF) == 0xD8 && (h[2] & 0xFF) == 0xFF) return "jpg";
        if ((h[0] & 0xFF) == 0x89 && h[1] == 'P' && h[2] == 'N' && h[3] == 'G') return "png";
        if (h[0] == 'R' && h[1] == 'I' && h[2] == 'F' && h[3] == 'F'
                && h[8] == 'W' && h[9] == 'E' && h[10] == 'B' && h[11] == 'P') return "webp";
        return null;
    }
}