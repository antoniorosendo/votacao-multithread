package common;

public class NetControl extends NetProtocol {
    private static final long serialVersionUID = 1L;
    private final NetCommand netCommand;

    public NetControl(NetCommand netCommand) {
        super();
        this.netCommand = netCommand;
    }

    public final NetCommand getNetCommand() {
        return netCommand;
    }
}