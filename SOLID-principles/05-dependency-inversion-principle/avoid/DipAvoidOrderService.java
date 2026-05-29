public class DipAvoidOrderService {
    private final DipAvoidMySqlOrderRepository orderRepository = new DipAvoidMySqlOrderRepository();

    public void placeOrder(DipAvoidOrder order) {
        orderRepository.save(order);
    }
}
