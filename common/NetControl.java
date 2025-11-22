package common;

public class NetControl extends NetProtocol {
    private static final long serialVersionUID = 1L;
    private final NetCommand netCommand;
    private final String message;

    public NetControl(NetCommand netCommand) {
        this(netCommand, "");
    }

    public NetControl(NetCommand netCommand, String message) {
        super();
        this.netCommand = netCommand;
        this.message = message;
    }

    public final NetCommand getNetCommand() {
        return netCommand;
    }

    public String getMessage() {
        return message;
    }
    
    @Override
    public String toString() {
        return netCommand.toString() + (message.isEmpty() ? "" : ": " + message);
    }
}