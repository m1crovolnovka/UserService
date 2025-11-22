package denis.userservice.exception;


import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

//    @ExceptionHandler(UserNotFoundException.class)
//    public ResponseEntity<String> handleUserNotFound(UserNotFoundException ex) {
//        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
//    }
//    @ExceptionHandler(CardNotFoundException.class)
//    public ResponseEntity<String> handleCardNotFound(CardNotFoundException ex) {
//        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
//    }
//
//    @ExceptionHandler(CardListFullException.class)
//    public ResponseEntity<String> handleBusiness(CardListFullException ex) {
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
//    }
//
//    @ExceptionHandler(EntityAlreadyExistsException.class)
//    public ResponseEntity<String> handleEntityExist(EntityAlreadyExistsException ex) {
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
//    }

//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<String> handleServerException(Exception ex) {
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
//    }

}
