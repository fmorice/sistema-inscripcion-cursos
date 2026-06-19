package com.transportista.guias.service.impl;

import com.transportista.guias.exception.StorageException;
import com.transportista.guias.service.S3Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.time.Year;

@Service
public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;
    private final String bucket;

    public S3ServiceImpl(S3Client s3Client,
                         @Value("${aws.s3.bucket}") String bucket) {
        this.s3Client = s3Client;
        this.bucket = bucket;
    }

    @Override
    public String subirArchivo(File archivo, String transportista) {
        String key = generarKey(archivo, transportista);

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType("application/pdf")
                    .build();

            s3Client.putObject(request, RequestBody.fromFile(archivo));
            return key;

        } catch (SdkException ex) {
            throw new StorageException("Error al subir el archivo a S3", ex);
        }
    }

    @Override
    public byte[] descargarArchivo(String key) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            return s3Client.getObjectAsBytes(request).asByteArray();

        } catch (NoSuchKeyException ex) {
            throw new StorageException(
                    "El archivo no existe en S3: " + key, ex);

        } catch (SdkException ex) {
            throw new StorageException(
                    "Error al descargar el archivo desde S3", ex);
        }
    }

    @Override
    public void actualizarArchivo(File archivo, String key) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType("application/pdf")
                    .build();

            s3Client.putObject(request, RequestBody.fromFile(archivo));

        } catch (SdkException ex) {
            throw new StorageException(
                    "Error al actualizar el archivo en S3", ex);
        }
    }

    @Override
    public void eliminarArchivo(String key) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            s3Client.deleteObject(request);

        } catch (SdkException ex) {
            throw new StorageException(
                    "Error al eliminar el archivo de S3", ex);
        }
    }

    @Override
    public String generarS3Uri(String key) {
        return String.format("s3://%s/%s", bucket, key);
    }

    private String generarKey(File archivo, String transportista) {
        String year = String.valueOf(Year.now().getValue());

        String nombreTransportista =
                transportista == null || transportista.isBlank()
                        ? "SinTransportista"
                        : normalizar(transportista);

        return String.format(
                "%s/%s/%s",
                year,
                nombreTransportista,
                archivo.getName()
        );
    }

    private String normalizar(String texto) {
        return texto
                .trim()
                .replaceAll("\\s+", "")
                .replaceAll("[^a-zA-Z0-9]", "");
    }
}