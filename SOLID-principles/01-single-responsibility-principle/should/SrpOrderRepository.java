import java.util.HashMap;
import java.util.Map;

public class SrpOrderRepository {
    private final Map<Long, SrpOrder> orders = new HashMap<>();

    public void save(SrpOrder order) {
        orders.put(order.getId(), order);
        System.out.println("Saved order: " + order.getId());
    }
}
