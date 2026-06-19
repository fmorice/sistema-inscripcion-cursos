package com.transportista.guias.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

/**
 * DTO para recibir los datos de creación y actualización de una inscripción de curso.
 */
@Getter
@Setter
@NoArgsConstructor
public class InscripcionCursoRequestDTO {

    @NotBlank(message = "El número de inscripción es obligatorio")
    private String numeroInscripcion;

    @NotBlank(message = "El estudiante es obligatorio")
    private String estudiante;

    @NotNull(message = "La fecha de inscripción es obligatoria")
    private LocalDate fechaInscripcion;

    @NotBlank(message = "El curso es obligatorio")
    private String curso;

    @NotBlank(message = "El nivel es obligatorio")
    private String nivel;

    @NotBlank(message = "El estado es obligatorio")
    private String estado;

    private String rutaArchivoS3;
}
