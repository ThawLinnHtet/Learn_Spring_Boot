public class SrpOrderService {
    private final SrpOrderValidator validator;
    private final SrpOrderRepository repository;
    private final SrpEmailNotifier emailNotifier;

    public SrpOrderService(
            SrpOrderValidator validator,
            SrpOrderRepository repository,
            SrpEmailNotifier emailNotifier
    ) {
        this.validator = validator;
        this.repository = repository;
        this.emailNotifier = emailNotifier;
    }

    public void placeOrder(SrpOrder order) {
        validator.validate(order);
        repository.save(order);
        emailNotifier.sendConfirmation(order);
    }
}
