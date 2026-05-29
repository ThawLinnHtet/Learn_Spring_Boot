import java.math.BigDecimal;

public class SrpAvoidOrderService {
    public void placeOrder(SrpAvoidOrder order) {
        if (order.getCustomerEmail() == null || order.getCustomerEmail().isBlank()) {
            throw new IllegalArgumentException("Customer email is required");
        }

        if (order.getTotal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Order total must be greater than zero");
        }

        System.out.println("Saving order to database: " + order.getId());
        System.out.println("Sending confirmation email to: " + order.getCustomerEmail());
    }
}

class SrpAvoidOrder {
    private final long id;
    private final String customerEmail;
    private final BigDecimal total;

    SrpAvoidOrder(long id, String customerEmail, BigDecimal total) {
        this.id = id;
        this.customerEmail = customerEmail;
        this.total = total;
    }

    long getId() {
        return id;
    }

    String getCustomerEmail() {
        return customerEmail;
    }

    BigDecimal getTotal() {
        return total;
    }
}
