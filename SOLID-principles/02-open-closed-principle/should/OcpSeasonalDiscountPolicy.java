import java.math.BigDecimal;

public class OcpSeasonalDiscountPolicy implements OcpDiscountPolicy {
    @Override
    public boolean supports(String customerType) {
        return "SEASONAL".equals(customerType);
    }

    @Override
    public BigDecimal apply(BigDecimal amount) {
        return amount.multiply(new BigDecimal("0.20"));
    }
}
