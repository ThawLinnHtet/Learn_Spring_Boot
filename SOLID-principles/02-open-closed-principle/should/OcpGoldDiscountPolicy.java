import java.math.BigDecimal;

public class OcpGoldDiscountPolicy implements OcpDiscountPolicy {
    @Override
    public boolean supports(String customerType) {
        return "GOLD".equals(customerType);
    }

    @Override
    public BigDecimal apply(BigDecimal amount) {
        return amount.multiply(new BigDecimal("0.10"));
    }
}
