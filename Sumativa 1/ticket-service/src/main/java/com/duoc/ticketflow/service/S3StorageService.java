package com.duoc.ticketflow.service;

import com.duoc.ticketflow.config.StorageProperties;
import com.duoc.ticketflow.exception.StorageException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.InputStream;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class S3StorageService {

    private static final Logger log = LoggerFactory.getLogger(S3StorageService.class);

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final StorageProperties properties;
    private String bucketName;

    public S3StorageService(S3Client s3Client, S3Presigner s3Presigner, StorageProperties properties) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        this.bucketName = properties.getAws().getS3().getBucketName();
        ensureBucketExists();
    }

    private void ensureBucketExists() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
            log.info("Bucket S3 verificado y disponible: {}", bucketName);
        } catch (NoSuchBucketException e) {
            log.warn("El bucket S3 '{}' no existe. Creándolo automáticamente...", bucketName);
            try {
                s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
                log.info("Bucket S3 '{}' creado exitosamente", bucketName);
            } catch (Exception ex) {
                log.error("Fallo al crear bucket S3 '{}': {}", bucketName, ex.getMessage());
            }
        } catch (Exception e) {
            log.warn("Verificación de bucket S3 '{}': {}", bucketName, e.getMessage());
        }
    }

    /**
     * Sube un ticket a S3 con clave estructurada (ej. evento123/usuarioX/ticket001.pdf)
     */
    public void uploadFile(String s3Key, byte[] content, String contentType, Map<String, String> metadata) {
        try {
            // Normalizar la clave para que no empiece con '/' en S3
            String normalizedKey = normalizeKey(s3Key);

            Map<String, String> safeMetadata = metadata != null ? metadata : new HashMap<>();
            safeMetadata.put("uploaded-by", "TicketFlow-CloudNative");

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(normalizedKey)
                    .contentType(contentType != null ? contentType : "application/pdf")
                    .metadata(safeMetadata)
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(content));
            log.info("Archivo subido exitosamente a S3: s3://{}/{}", bucketName, normalizedKey);
        } catch (Exception e) {
            throw new StorageException("Error al subir archivo a Amazon S3 [" + s3Key + "]", e);
        }
    }

    /**
     * Modifica y actualiza un archivo en AWS S3
     */
    public void updateFile(String s3Key, byte[] newContent, String contentType, Map<String, String> updatedMetadata) {
        try {
            String normalizedKey = normalizeKey(s3Key);

            Map<String, String> safeMetadata = updatedMetadata != null ? updatedMetadata : new HashMap<>();
            safeMetadata.put("last-updated-by", "TicketFlow-CloudNative");
            safeMetadata.put("version-status", "updated");

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(normalizedKey)
                    .contentType(contentType != null ? contentType : "application/pdf")
                    .metadata(safeMetadata)
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(newContent));
            log.info("Archivo actualizado con éxito en S3: s3://{}/{}", bucketName, normalizedKey);
        } catch (Exception e) {
            throw new StorageException("Error al actualizar archivo en Amazon S3 [" + s3Key + "]", e);
        }
    }

    /**
     * Descarga el archivo desde AWS S3 como array de bytes
     */
    public byte[] downloadFile(String s3Key) {
        try {
            String normalizedKey = normalizeKey(s3Key);
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(normalizedKey)
                    .build();

            ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getRequest);
            return s3Object.readAllBytes();
        } catch (NoSuchKeyException e) {
            throw new StorageException("El archivo no existe en Amazon S3: " + s3Key);
        } catch (Exception e) {
            throw new StorageException("Error al descargar archivo desde Amazon S3 [" + s3Key + "]", e);
        }
    }

    /**
     * Descarga como InputStream para streaming eficiente
     */
    public InputStream downloadStream(String s3Key) {
        try {
            String normalizedKey = normalizeKey(s3Key);
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(normalizedKey)
                    .build();

            return s3Client.getObject(getRequest);
        } catch (Exception e) {
            throw new StorageException("Error al obtener stream de descarga desde S3 [" + s3Key + "]", e);
        }
    }

    /**
     * Genera una URL prefirmada (Pre-signed URL) para descarga directa y segura desde S3
     */
    public String generatePresignedDownloadUrl(String s3Key, int durationMinutes) {
        try {
            String normalizedKey = normalizeKey(s3Key);
            int expiration = durationMinutes > 0 ? durationMinutes : properties.getAws().getS3().getPresignedUrlExpirationMinutes();

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(normalizedKey)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(expiration))
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            return presignedRequest.url().toString();
        } catch (Exception e) {
            log.warn("No se pudo generar Presigned URL para S3 [{}]: {}", s3Key, e.getMessage());
            return null;
        }
    }

    /**
     * Elimina un archivo de AWS S3
     */
    public void deleteFile(String s3Key) {
        try {
            String normalizedKey = normalizeKey(s3Key);
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(normalizedKey)
                    .build();

            s3Client.deleteObject(deleteRequest);
            log.info("Archivo eliminado exitosamente de S3: s3://{}/{}", bucketName, normalizedKey);
        } catch (Exception e) {
            log.warn("Fallo al eliminar archivo de S3 [{}]: {}", s3Key, e.getMessage());
        }
    }

    /**
     * Verifica si existe un objeto en S3
     */
    public boolean existsInS3(String s3Key) {
        try {
            String normalizedKey = normalizeKey(s3Key);
            s3Client.headObject(HeadObjectRequest.builder().bucket(bucketName).key(normalizedKey).build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public String getBucketName() {
        return bucketName;
    }

    private String normalizeKey(String key) {
        if (key == null) return "";
        return key.startsWith("/") ? key.substring(1) : key;
    }
}
