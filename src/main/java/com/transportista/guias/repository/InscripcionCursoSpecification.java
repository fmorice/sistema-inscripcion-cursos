package com.transportista.guias.repository;

import com.transportista.guias.entity.InscripcionCurso;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Locale;

public final class InscripcionCursoSpecification {

    private InscripcionCursoSpecification() {
    }

    public static Specification<InscripcionCurso> tieneArchivoEnS3() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isNotNull(root.get("rutaArchivoS3"));
    }

    public static Specification<InscripcionCurso> estudianteIgual(String estudiante) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("estudiante")),
                        estudiante.toLowerCase(Locale.ROOT)
                );
    }

    public static Specification<InscripcionCurso> fechaMayorOIgual(LocalDate fechaDesde) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("fechaInscripcion"), fechaDesde);
    }

    public static Specification<InscripcionCurso> fechaMenorOIgual(LocalDate fechaHasta) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get("fechaInscripcion"), fechaHasta);
    }

    public static Specification<InscripcionCurso> estadoIgual(String estado) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("estado")),
                        estado.toLowerCase(Locale.ROOT)
                );
    }
}
