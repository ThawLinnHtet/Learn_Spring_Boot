import java.math.BigDecimal;

public class SrpOrderValidator {
    public void validate(SrpOrder order) {
        if (order.getCustomerEmail() == null || order.getCustomerEmail().isBlank()) {
            throw new IllegalArgumentException("Customer email is required");
        }

        if (order.getTotal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Order total must be greater than zero");
        }
    }
}
