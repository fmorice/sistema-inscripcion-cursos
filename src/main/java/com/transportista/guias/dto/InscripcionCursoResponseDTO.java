package com.transportista.guias.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de respuesta que devuelve información de una inscripción de curso.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InscripcionCursoResponseDTO {

    private Long id;
    private String numeroInscripcion;
    private String estudiante;
    private LocalDate fechaInscripcion;
    private String curso;
    private String nivel;
    private String estado;
    private String rutaArchivoS3;
    private LocalDateTime fechaCreacion;
}
