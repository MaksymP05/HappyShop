package ci553.happyshop.catalogue.exception;

// number of products must be < 50 exception
public class ExcessiveOrderQuantityException extends Exception {
    public ExcessiveOrderQuantityException(String message) {
        super(message);
    }
}
