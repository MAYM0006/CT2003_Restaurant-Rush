package restaurantrush.exception;

/** Signals a rejected manager action without consuming a staff turn. */
public class InvalidGameActionException extends RuntimeException {
    public InvalidGameActionException(String message) { 
        super(message); 
    }
}