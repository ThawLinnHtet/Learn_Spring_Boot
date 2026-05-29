import java.math.BigDecimal;
import java.util.List;

public class OcpDiscountCalculator {
    private final List<OcpDiscountPolicy> discountPolicies;

    public OcpDiscountCalculator(List<OcpDiscountPolicy> discountPolicies) {
        this.discountPolicies = discountPolicies;
    }

    public BigDecimal calculate(String customerType, BigDecimal amount) {
        return discountPolicies.stream()
                .filter(policy -> policy.supports(customerType))
                .findFirst()
                .map(policy -> policy.apply(amount))
                .orElse(BigDecimal.ZERO);
    }
}
