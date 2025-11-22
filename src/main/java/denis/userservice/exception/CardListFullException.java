package denis.userservice.exception;

public class CardListFullException extends RuntimeException {
    public CardListFullException(String message) {
        super(message);
    }
}
