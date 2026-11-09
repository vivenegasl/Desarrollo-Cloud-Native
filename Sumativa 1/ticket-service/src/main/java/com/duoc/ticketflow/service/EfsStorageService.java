package com.duoc.ticketflow.service;

import com.duoc.ticketflow.config.StorageProperties;
import com.duoc.ticketflow.exception.StorageException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Service
public class EfsStorageService {

    private static final Logger log = LoggerFactory.getLogger(EfsStorageService.class);

    private final StorageProperties properties;
    private Path rootMountPath;

    public EfsStorageService(StorageProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        String mountPathStr = properties.getStorage().getEfs().getMountPath();
        this.rootMountPath = Paths.get(mountPathStr).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootMountPath);
            log.info("Directorio de montaje Amazon EFS inicializado en: {}", this.rootMountPath);
        } catch (IOException e) {
            log.error("No se pudo crear o verificar el directorio de montaje EFS: {}", this.rootMountPath, e);
            throw new StorageException("No se pudo inicializar el almacenamiento temporal EFS en: " + this.rootMountPath, e);
        }
    }

    public Path saveTicketToEfs(String eventCode, String userId, String ticketCode, byte[] pdfBytes) {
        try {
            Path targetDir = resolveDirectory(eventCode, userId);
            Files.createDirectories(targetDir);

            Path targetFile = targetDir.resolve(ticketCode + ".pdf");
            Files.write(targetFile, pdfBytes,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);

            log.info("Ticket temporal almacenado con éxito en EFS: {}", targetFile);
            return targetFile;
        } catch (IOException e) {
            throw new StorageException("Fallo al guardar ticket en EFS: " + ticketCode, e);
        }
    }

    public byte[] readTicketFromEfs(String eventCode, String userId, String ticketCode) {
        try {
            Path targetFile = resolveTicketPath(eventCode, userId, ticketCode);
            if (!Files.exists(targetFile)) {
                throw new StorageException("El archivo del ticket no existe en EFS: " + targetFile);
            }
            return Files.readAllBytes(targetFile);
        } catch (IOException e) {
            throw new StorageException("Error leyendo ticket desde EFS: " + ticketCode, e);
        }
    }

    public boolean deleteTicketFromEfs(String eventCode, String userId, String ticketCode) {
        try {
            Path targetFile = resolveTicketPath(eventCode, userId, ticketCode);
            boolean deleted = Files.deleteIfExists(targetFile);
            log.info("Ticket eliminado de EFS [{}]: {}", deleted, targetFile);
            return deleted;
        } catch (IOException e) {
            log.warn("No se pudo eliminar ticket de EFS: {}", ticketCode, e);
            return false;
        }
    }

    public boolean existsInEfs(String eventCode, String userId, String ticketCode) {
        Path targetFile = resolveTicketPath(eventCode, userId, ticketCode);
        return Files.exists(targetFile);
    }

    public long getFileSize(String eventCode, String userId, String ticketCode) {
        try {
            Path targetFile = resolveTicketPath(eventCode, userId, ticketCode);
            if (Files.exists(targetFile)) {
                return Files.size(targetFile);
            }
            return 0L;
        } catch (IOException e) {
            return 0L;
        }
    }

    public String getTicketCanonicalPath(String eventCode, String userId, String ticketCode) {
        return resolveTicketPath(eventCode, userId, ticketCode).toString();
    }

    public Path getRootMountPath() {
        return rootMountPath;
    }

    private Path resolveDirectory(String eventCode, String userId) {
        return rootMountPath.resolve(sanitize(eventCode)).resolve(sanitize(userId));
    }

    private Path resolveTicketPath(String eventCode, String userId, String ticketCode) {
        return resolveDirectory(eventCode, userId).resolve(sanitize(ticketCode) + ".pdf");
    }

    private String sanitize(String input) {
        if (input == null) return "general";
        return input.replaceAll("[^a-zA-Z0-9_-]", "_");
    }
}
