public class SysCallRequest {
    public enum Type {
        READ,
        WRITE,
        INVALID
    }

    public final GerenteProcessos.PCB processo;
    public final Type type;
    public final int address;
    public final int value;

    public SysCallRequest(GerenteProcessos.PCB processo, Type type, int address, int value) {
        this.processo = processo;
        this.type = type;
        this.address = address;
        this.value = value;
    }
}
