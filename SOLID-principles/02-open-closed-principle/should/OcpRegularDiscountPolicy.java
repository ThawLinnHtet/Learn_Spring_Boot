import java.math.BigDecimal;

public class OcpRegularDiscountPolicy implements OcpDiscountPolicy {
    @Override
    public boolean supports(String customerType) {
        return "REGULAR".equals(customerType);
    }

    @Override
    public BigDecimal apply(BigDecimal amount) {
        return amount.multiply(new BigDecimal("0.05"));
    }
}
