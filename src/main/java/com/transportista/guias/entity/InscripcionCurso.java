package com.transportista.guias.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad principal que representa una inscripción de curso en el sistema.
 */
@Entity
@Table(name = "inscripcion_cursos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InscripcionCurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_inscripcion", nullable = false, unique = true)
    private String numeroInscripcion;

    @Column(nullable = false)
    private String estudiante;

    @Column(nullable = false)
    private LocalDate fechaInscripcion;

    @Column(nullable = false)
    private String curso;

    @Column(nullable = false)
    private String nivel;

    @Column(nullable = false)
    private String estado;

    @Column(name = "ruta_archivo_s3")
    private String rutaArchivoS3;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    public void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }
}
