import java.math.BigDecimal;

public class OcpAvoidDiscountCalculator {
    public BigDecimal calculate(String customerType, BigDecimal amount) {
        if ("REGULAR".equals(customerType)) {
            return amount.multiply(new BigDecimal("0.05"));
        }

        if ("GOLD".equals(customerType)) {
            return amount.multiply(new BigDecimal("0.10"));
        }

        if ("SEASONAL".equals(customerType)) {
            return amount.multiply(new BigDecimal("0.20"));
        }

        return BigDecimal.ZERO;
    }
}
