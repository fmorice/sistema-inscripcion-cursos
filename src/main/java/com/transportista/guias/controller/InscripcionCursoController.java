package com.transportista.guias.controller;

import com.transportista.guias.dto.InscripcionCursoRequestDTO;
import com.transportista.guias.dto.InscripcionCursoResponseDTO;
import com.transportista.guias.exception.RecursoNoEncontradoException;
import com.transportista.guias.service.InscripcionCursoService;
import com.transportista.guias.service.S3Service;
import jakarta.validation.Valid;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador para gestión de inscripciones de cursos
 * Todos los endpoints están protegidos por Spring Security (OAuth2 con Azure AD B2C)
 * Se requiere un JWT válido emitido por Azure AD B2C
 */
@RestController
@RequestMapping("/inscripciones")
public class InscripcionCursoController {

    private final InscripcionCursoService inscripcionCursoService;
    private final S3Service s3Service;

    public InscripcionCursoController(InscripcionCursoService inscripcionCursoService, S3Service s3Service) {
        this.inscripcionCursoService = inscripcionCursoService;
        this.s3Service = s3Service;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<InscripcionCursoResponseDTO> crearInscripcion(@Valid @RequestBody InscripcionCursoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inscripcionCursoService.crearInscripcion(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<List<InscripcionCursoResponseDTO>> obtenerInscripciones() {
        return ResponseEntity.ok(inscripcionCursoService.obtenerInscripciones());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<InscripcionCursoResponseDTO> obtenerInscripcionPorId(@PathVariable Long id) {
        return ResponseEntity.ok(inscripcionCursoService.obtenerInscripcionPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<InscripcionCursoResponseDTO> actualizarInscripcion(
            @PathVariable Long id,
            @Valid @RequestBody InscripcionCursoRequestDTO request) {

        return ResponseEntity.ok(inscripcionCursoService.actualizarInscripcion(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> eliminarInscripcion(@PathVariable Long id) {
        inscripcionCursoService.eliminarInscripcion(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/subir-s3")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<Map<String, String>> subirArchivoAS3(@PathVariable Long id) {

        InscripcionCursoResponseDTO inscripcion = inscripcionCursoService.obtenerInscripcionPorId(id);
        File archivoTemporal = generarPdfTemporal(inscripcion);

        try {
            String key = s3Service.subirArchivo(archivoTemporal, inscripcion.getEstudiante());
            inscripcionCursoService.actualizarRutaArchivoS3(id, key);

            Map<String, String> respuesta = new HashMap<>();
            respuesta.put("key", key);
            respuesta.put("url", s3Service.generarS3Uri(key));

            return ResponseEntity.ok(respuesta);

        } finally {
            if (archivoTemporal.exists()) {
                archivoTemporal.delete();
            }
        }
    }

    @GetMapping("/{id}/descargar")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<byte[]> descargarArchivo(@PathVariable Long id) {

        InscripcionCursoResponseDTO inscripcion = inscripcionCursoService.obtenerInscripcionPorId(id);
        String key = inscripcion.getRutaArchivoS3();

        if (key == null || key.isBlank()) {
            throw new RecursoNoEncontradoException("La inscripción no tiene archivo en S3");
        }

        byte[] contenido = s3Service.descargarArchivo(key);
        String nombreArchivo = key.substring(key.lastIndexOf("/") + 1);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(contenido);
    }

    @GetMapping("/historial")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<List<InscripcionCursoResponseDTO>> historialArchivos(
            @RequestParam(required = false) String estudiante,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) String estado) {

        return ResponseEntity.ok(
                inscripcionCursoService.obtenerHistorialArchivos(
                        estudiante, fechaDesde, fechaHasta, estado
                )
        );
    }

    @GetMapping("/buscar")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<List<InscripcionCursoResponseDTO>> buscarInscripciones(
            @RequestParam String estudiante,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        return ResponseEntity.ok(
                inscripcionCursoService.buscarInscripciones(estudiante, fecha)
        );
    }

    /**
     * GENERACIÓN PDF (FIX PDFBox 2.0.30)
     */
    private File generarPdfTemporal(InscripcionCursoResponseDTO inscripcion) {
        try {
            File archivo = File.createTempFile("inscripcion-" + inscripcion.getId() + "-", ".pdf");

            try (PDDocument document = new PDDocument()) {

                PDPage page = new PDPage();
                document.addPage(page);

                try (PDPageContentStream content = new PDPageContentStream(document, page)) {

                    content.beginText();
                    content.setFont(PDType1Font.HELVETICA_BOLD, 14);
                    content.newLineAtOffset(40, 750);
                    content.showText("Inscripción de Curso");

                    content.newLineAtOffset(0, -20);
                    content.setFont(PDType1Font.HELVETICA, 12);

                    content.showText("Número: " + inscripcion.getNumeroInscripcion());
                    content.newLineAtOffset(0, -15);
                    content.showText("Estudiante: " + inscripcion.getEstudiante());
                    content.newLineAtOffset(0, -15);
                    content.showText("Fecha: " + inscripcion.getFechaInscripcion());
                    content.newLineAtOffset(0, -15);
                    content.showText("Curso: " + inscripcion.getCurso());
                    content.newLineAtOffset(0, -15);
                    content.showText("Nivel: " + inscripcion.getNivel());
                    content.newLineAtOffset(0, -15);
                    content.showText("Estado: " + inscripcion.getEstado());

                    content.endText();
                }

                document.save(archivo);
            }

            return archivo;

        } catch (IOException ex) {
            throw new RuntimeException("Error al generar PDF temporal", ex);
        }
    }
}
