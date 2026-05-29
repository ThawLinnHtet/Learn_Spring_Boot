import java.math.BigDecimal;

public interface OcpDiscountPolicy {
    boolean supports(String customerType);

    BigDecimal apply(BigDecimal amount);
}
