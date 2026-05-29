public class DipAvoidMySqlOrderRepository {
    public void save(DipAvoidOrder order) {
        System.out.println("Saving order to MySQL: " + order.getId());
    }
}
