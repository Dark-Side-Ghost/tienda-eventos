package com.udb.tienda.excepcion;

import com.udb.tienda.dto.RespuestaError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Centraliza el manejo de errores de toda la API para que las
 * respuestas sean siempre claras y con el codigo de estado correcto:
 *
 *  - 400 Bad Request: datos invalidos (fallo alguna validacion @NotBlank, @Positive, etc.)
 *  - 404 Not Found: se pidio un producto que no existe.
 *  - 500 Internal Server Error: cualquier otro error no esperado.
 */
@RestControllerAdvice
@Slf4j
public class ManejadorGlobalExcepciones {

    @ExceptionHandler(RecursoNoEncontradoExcepcion.class)
    public ResponseEntity<RespuestaError> manejarNoEncontrado(RecursoNoEncontradoExcepcion ex) {
        RespuestaError cuerpo = new RespuestaError(
                HttpStatus.NOT_FOUND.value(),
                "No encontrado",
                ex.getMessage(),
                List.of()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(cuerpo);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespuestaError> manejarValidacion(MethodArgumentNotValidException ex) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .toList();

        RespuestaError cuerpo = new RespuestaError(
                HttpStatus.BAD_REQUEST.value(),
                "Solicitud invalida",
                "Uno o mas campos no son validos",
                detalles
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(cuerpo);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespuestaError> manejarErrorGeneral(Exception ex) {
        log.error("Error inesperado: ", ex);
        RespuestaError cuerpo = new RespuestaError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error interno",
                "Ocurrio un error inesperado en el servidor",
                List.of()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(cuerpo);
    }
}
