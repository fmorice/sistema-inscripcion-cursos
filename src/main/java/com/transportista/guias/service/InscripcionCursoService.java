package com.transportista.guias.service;

import com.transportista.guias.dto.InscripcionCursoRequestDTO;
import com.transportista.guias.dto.InscripcionCursoResponseDTO;

import java.time.LocalDate;
import java.util.List;

public interface InscripcionCursoService {

    InscripcionCursoResponseDTO crearInscripcion(InscripcionCursoRequestDTO request);

    List<InscripcionCursoResponseDTO> obtenerInscripciones();

    InscripcionCursoResponseDTO obtenerInscripcionPorId(Long id);

    InscripcionCursoResponseDTO actualizarInscripcion(Long id, InscripcionCursoRequestDTO request);

    InscripcionCursoResponseDTO actualizarRutaArchivoS3(Long id, String rutaArchivoS3);

    void eliminarInscripcion(Long id);

    List<InscripcionCursoResponseDTO> buscarInscripciones(String estudiante, LocalDate fecha);

    List<InscripcionCursoResponseDTO> obtenerHistorialArchivos(String estudiante,
                                                 LocalDate fechaDesde,
                                                 LocalDate fechaHasta,
                                                 String estado);
}
