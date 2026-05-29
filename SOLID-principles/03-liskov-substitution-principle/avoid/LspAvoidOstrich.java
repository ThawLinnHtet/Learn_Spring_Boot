public class LspAvoidOstrich extends LspAvoidBird {
    @Override
    public void fly() {
        throw new UnsupportedOperationException("Ostriches cannot fly");
    }
}
