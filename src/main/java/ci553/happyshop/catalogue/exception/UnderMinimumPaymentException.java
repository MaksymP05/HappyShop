package ci553.happyshop.catalogue.exception;

public class UnderMinimumPaymentException extends Exception {
    public UnderMinimumPaymentException(String message) {
        super(message);
    }
}
