package common;

// Adicionado 'public'
public enum NetCommand {
    Acknowledge("Acknowledge Command"),
    Shutdown("Shutdown Command");

    private final String description;

    private NetCommand(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return (this.description);
    }
}