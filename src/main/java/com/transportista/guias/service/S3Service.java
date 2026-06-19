package com.transportista.guias.service;

import java.io.File;

/**
 * Servicio de almacenamiento en AWS S3.
 */
public interface S3Service {

    String subirArchivo(File archivo, String transportista);

    byte[] descargarArchivo(String key);

    void actualizarArchivo(File archivo, String key);

    void eliminarArchivo(String key);

    String generarS3Uri(String key);
}
