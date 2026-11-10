package cn.sparrowmini.web;

import jakarta.persistence.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestControllerAdvice
public class ApiErrors {
    @ExceptionHandler(SecurityException.class) ResponseEntity<?> forbidden(SecurityException e){return ResponseEntity.status(403).body(Map.of("code","ACCESS_DENIED"));}
    @ExceptionHandler(OptimisticLockException.class) ResponseEntity<?> conflict(OptimisticLockException e){return ResponseEntity.status(409).body(Map.of("code","STALE_VERSION"));}
    @ExceptionHandler({IllegalArgumentException.class,org.springframework.http.converter.HttpMessageNotReadableException.class}) ResponseEntity<?> invalid(Exception e){return ResponseEntity.badRequest().body(Map.of("code","INVALID_REQUEST"));}
    @ExceptionHandler({IllegalStateException.class,UnsupportedOperationException.class}) ResponseEntity<?> state(RuntimeException e){return ResponseEntity.status(409).body(Map.of("code","OPERATION_UNAVAILABLE"));}
}
