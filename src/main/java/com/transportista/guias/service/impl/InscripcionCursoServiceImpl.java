package com.transportista.guias.service.impl;

import com.transportista.guias.dto.InscripcionCursoRequestDTO;
import com.transportista.guias.dto.InscripcionCursoResponseDTO;
import com.transportista.guias.entity.InscripcionCurso;
import com.transportista.guias.exception.RecursoNoEncontradoException;
import com.transportista.guias.repository.InscripcionCursoRepository;
import com.transportista.guias.repository.InscripcionCursoSpecification;
import com.transportista.guias.service.InscripcionCursoService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class InscripcionCursoServiceImpl implements InscripcionCursoService {

    private final InscripcionCursoRepository inscripcionCursoRepository;

    public InscripcionCursoServiceImpl(InscripcionCursoRepository inscripcionCursoRepository) {
        this.inscripcionCursoRepository = inscripcionCursoRepository;
    }

    @Override
    public InscripcionCursoResponseDTO crearInscripcion(InscripcionCursoRequestDTO request) {
        InscripcionCurso inscripcion = mapToEntity(request);
        InscripcionCurso guardada = inscripcionCursoRepository.save(inscripcion);
        return mapToResponse(guardada);
    }

    @Override
    public List<InscripcionCursoResponseDTO> obtenerInscripciones() {
        return inscripcionCursoRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public InscripcionCursoResponseDTO obtenerInscripcionPorId(Long id) {
        InscripcionCurso inscripcion = inscripcionCursoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Inscripción no encontrada con id: " + id));
        return mapToResponse(inscripcion);
    }

    @Override
    public InscripcionCursoResponseDTO actualizarInscripcion(Long id, InscripcionCursoRequestDTO request) {
        InscripcionCurso inscripcionExistente = inscripcionCursoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Inscripción no encontrada con id: " + id));

        inscripcionExistente.setNumeroInscripcion(request.getNumeroInscripcion());
        inscripcionExistente.setEstudiante(request.getEstudiante());
        inscripcionExistente.setFechaInscripcion(request.getFechaInscripcion());
        inscripcionExistente.setCurso(request.getCurso());
        inscripcionExistente.setNivel(request.getNivel());
        inscripcionExistente.setEstado(request.getEstado());
        inscripcionExistente.setRutaArchivoS3(request.getRutaArchivoS3());

        InscripcionCurso actualizada = inscripcionCursoRepository.save(inscripcionExistente);
        return mapToResponse(actualizada);
    }

    @Override
    public InscripcionCursoResponseDTO actualizarRutaArchivoS3(Long id, String rutaArchivoS3) {
        InscripcionCurso inscripcionExistente = inscripcionCursoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Inscripción no encontrada con id: " + id));

        inscripcionExistente.setRutaArchivoS3(rutaArchivoS3);
        InscripcionCurso actualizada = inscripcionCursoRepository.save(inscripcionExistente);
        return mapToResponse(actualizada);
    }

    @Override
    public void eliminarInscripcion(Long id) {
        if (!inscripcionCursoRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Inscripción no encontrada con id: " + id);
        }
        inscripcionCursoRepository.deleteById(id);
    }

    @Override
    public List<InscripcionCursoResponseDTO> buscarInscripciones(String estudiante, LocalDate fecha) {
        return inscripcionCursoRepository.findByEstudianteIgnoreCaseAndFechaInscripcion(estudiante, fecha)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<InscripcionCursoResponseDTO> obtenerHistorialArchivos(String estudiante, LocalDate fechaDesde, LocalDate fechaHasta, String estado) {
        Specification<InscripcionCurso> specification = Specification.where(InscripcionCursoSpecification.tieneArchivoEnS3());

        if (estudiante != null && !estudiante.isBlank()) {
            specification = specification.and(InscripcionCursoSpecification.estudianteIgual(estudiante));
        }
        if (fechaDesde != null) {
            specification = specification.and(InscripcionCursoSpecification.fechaMayorOIgual(fechaDesde));
        }
        if (fechaHasta != null) {
            specification = specification.and(InscripcionCursoSpecification.fechaMenorOIgual(fechaHasta));
        }
        if (estado != null && !estado.isBlank()) {
            specification = specification.and(InscripcionCursoSpecification.estadoIgual(estado));
        }

        return inscripcionCursoRepository.findAll(specification)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private InscripcionCurso mapToEntity(InscripcionCursoRequestDTO request) {
        return InscripcionCurso.builder()
                .numeroInscripcion(request.getNumeroInscripcion())
                .estudiante(request.getEstudiante())
                .fechaInscripcion(request.getFechaInscripcion())
                .curso(request.getCurso())
                .nivel(request.getNivel())
                .estado(request.getEstado())
                .rutaArchivoS3(request.getRutaArchivoS3())
                .build();
    }

    private InscripcionCursoResponseDTO mapToResponse(InscripcionCurso inscripcion) {
        return new InscripcionCursoResponseDTO(
                inscripcion.getId(),
                inscripcion.getNumeroInscripcion(),
                inscripcion.getEstudiante(),
                inscripcion.getFechaInscripcion(),
                inscripcion.getCurso(),
                inscripcion.getNivel(),
                inscripcion.getEstado(),
                inscripcion.getRutaArchivoS3(),
                inscripcion.getFechaCreacion()
        );
    }
}
