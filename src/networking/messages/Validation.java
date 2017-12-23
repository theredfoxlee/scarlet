package networking.messages;

public class Validation extends Message {
    private final boolean valid;

    public Validation(boolean valid) {
        this.valid = valid;
    }

    public boolean isValid() {
        return valid;
    }
}
