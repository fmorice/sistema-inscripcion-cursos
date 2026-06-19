package com.transportista.guias.repository;

import com.transportista.guias.entity.InscripcionCurso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio JPA para la entidad InscripcionCurso.
 */
@Repository
public interface InscripcionCursoRepository extends JpaRepository<InscripcionCurso, Long>, JpaSpecificationExecutor<InscripcionCurso> {

    List<InscripcionCurso> findByEstudianteIgnoreCaseAndFechaInscripcion(String estudiante, LocalDate fechaInscripcion);
}
