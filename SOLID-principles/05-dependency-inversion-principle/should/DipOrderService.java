public class DipOrderService {
    private final DipOrderRepository orderRepository;

    public DipOrderService(DipOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public void placeOrder(DipOrder order) {
        orderRepository.save(order);
    }
}
