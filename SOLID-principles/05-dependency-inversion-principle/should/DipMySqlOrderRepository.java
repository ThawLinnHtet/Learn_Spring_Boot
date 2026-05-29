public class DipMySqlOrderRepository implements DipOrderRepository {
    @Override
    public void save(DipOrder order) {
        System.out.println("Saving order to MySQL: " + order.getId());
    }
}
