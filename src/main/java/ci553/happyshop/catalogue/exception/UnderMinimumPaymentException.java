package ci553.happyshop.catalogue.exception;

//payment can't be less than 5 pounds exception
public class UnderMinimumPaymentException extends Exception {
    public UnderMinimumPaymentException(String message) {
        super(message);
    }
}
