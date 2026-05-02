package br.com.oficina.shared.api;

import br.com.oficina.shared.domain.BusinessRuleException;
import br.com.oficina.shared.domain.NotFoundException;
import br.com.oficina.shared.domain.ValidationException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        ProblemDetail pd = initProblem(HttpStatus.BAD_REQUEST, "Validation error", "Requisicao invalida");

        List<Map<String, Object>> errors = new ArrayList<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            Map<String, Object> e = new LinkedHashMap<>();
            e.put("field", fe.getField());
            e.put("message", fe.getDefaultMessage());
            errors.add(e);
        }

        pd.setProperty("errors", errors);
        enrich(pd);
        return ResponseEntity.status(pd.getStatus()).body(pd);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(ConstraintViolationException ex) {
        ProblemDetail pd = initProblem(HttpStatus.BAD_REQUEST, "Validation error", "Requisicao invalida");
        pd.setProperty("violations", ex.getConstraintViolations().stream().map(v -> Map.of(
                "path", String.valueOf(v.getPropertyPath()),
                "message", v.getMessage()
        )).toList());
        enrich(pd);
        return ResponseEntity.status(pd.getStatus()).body(pd);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ProblemDetail> handleDomainValidation(ValidationException ex) {
        ProblemDetail pd = initProblem(HttpStatus.BAD_REQUEST, "Validation error", ex.getMessage());
        enrich(pd);
        return ResponseEntity.status(pd.getStatus()).body(pd);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail pd = initProblem(HttpStatus.BAD_REQUEST, "Validation error", ex.getMessage());
        enrich(pd);
        return ResponseEntity.status(pd.getStatus()).body(pd);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ProblemDetail> handleBusinessRule(BusinessRuleException ex) {
        ProblemDetail pd = initProblem(HttpStatus.CONFLICT, "Business rule violation", ex.getMessage());
        enrich(pd);
        return ResponseEntity.status(pd.getStatus()).body(pd);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(NotFoundException ex) {
        ProblemDetail pd = initProblem(HttpStatus.NOT_FOUND, "Not found", ex.getMessage());
        enrich(pd);
        return ResponseEntity.status(pd.getStatus()).body(pd);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrity(DataIntegrityViolationException ex) {
        ProblemDetail pd = initProblem(HttpStatus.CONFLICT, "Data integrity violation", "Violacao de integridade de dados");
        enrich(pd);
        return ResponseEntity.status(pd.getStatus()).body(pd);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleNotReadable(HttpMessageNotReadableException ex) {
        ProblemDetail pd = initProblem(HttpStatus.BAD_REQUEST, "Malformed request", "Corpo da requisicao invalido ou malformado");
        enrich(pd);
        return ResponseEntity.status(pd.getStatus()).body(pd);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGeneric(Exception ex) {
        log.error("Unhandled error", ex);
        ProblemDetail pd = initProblem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error", "Erro interno");
        enrich(pd);
        return ResponseEntity.status(pd.getStatus()).body(pd);
    }

    private static ProblemDetail initProblem(HttpStatus status, String title, String detail) {
        ProblemDetail pd = ProblemDetail.forStatus(status);
        pd.setTitle(title);
        pd.setDetail(detail);
        return pd;
    }

    private static void enrich(ProblemDetail pd) {
        String correlationId = MDC.get("correlationId");
        if (correlationId != null && !correlationId.isBlank()) {
            pd.setProperty("correlationId", correlationId);
        }
        pd.setProperty("timestamp", OffsetDateTime.now().toString());
    }
}
