package restaurantrush.exception;

public class InvalidGameActionException extends RuntimeException {
    public InvalidGameActionException(String message) { 
        super(message); 
    }
}