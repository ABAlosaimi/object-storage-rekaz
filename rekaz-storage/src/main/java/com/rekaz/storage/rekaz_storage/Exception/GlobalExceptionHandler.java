package com.rekaz.storage.rekaz_storage.Exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    
        @ExceptionHandler(BlobNotFoundException.class)
        public ResponseEntity<Object> handleBlobNotFoundException(BlobNotFoundException ex) {
            return ResponseEntity.status(404).body("Blob not found: " + ex.getMessage());
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
            return ResponseEntity.status(400).body("Bad request, IllegalArgumentException: " + ex.getMessage());
        }

        @ExceptionHandler(FailLocalStorageException.class)
        public ResponseEntity<Object> handleFailLocalStorageException(FailLocalStorageException ex) {
            return ResponseEntity.status(500).body("Internal Server Error, FailLocalStorageException: " + ex.getMessage());
        }

        @ExceptionHandler(UnauthorizedException.class)
        public ResponseEntity<Object> handleUnauthorizedException(UnauthorizedException ex) {
            return ResponseEntity.status(403).body("Unauthorized: " + ex.getMessage());
        }
}
