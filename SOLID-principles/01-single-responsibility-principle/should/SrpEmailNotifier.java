public class SrpEmailNotifier {
    public void sendConfirmation(SrpOrder order) {
        System.out.println("Sending confirmation email to: " + order.getCustomerEmail());
    }
}
