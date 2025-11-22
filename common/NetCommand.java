package common;

public enum NetCommand {
    Acknowledge("Acknowledge Command"),
    Shutdown("Shutdown Command"),
    CMD_OK("Command OK"),
    CMD_ERROR("Command Error");

    private final String description;

    private NetCommand(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return (this.description);
    }
}