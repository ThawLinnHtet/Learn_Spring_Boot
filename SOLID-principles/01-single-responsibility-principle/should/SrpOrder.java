import java.math.BigDecimal;

public class SrpOrder {
    private final long id;
    private final String customerEmail;
    private final BigDecimal total;

    public SrpOrder(long id, String customerEmail, BigDecimal total) {
        this.id = id;
        this.customerEmail = customerEmail;
        this.total = total;
    }

    public long getId() {
        return id;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public BigDecimal getTotal() {
        return total;
    }
}
