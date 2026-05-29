public class IspAvoidRobotWorker implements IspAvoidWorker {
    @Override
    public void work() {
        System.out.println("Robot is working");
    }

    @Override
    public void eat() {
        throw new UnsupportedOperationException("Robot does not eat");
    }

    @Override
    public void sleep() {
        throw new UnsupportedOperationException("Robot does not sleep");
    }
}
