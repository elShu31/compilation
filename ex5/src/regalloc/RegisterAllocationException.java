package regalloc;

public class RegisterAllocationException extends RuntimeException {
    public RegisterAllocationException(String message) {
        super(message);
    }
}
