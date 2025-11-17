public class CPUResult {
    public final Interrupts interrupt;
    public final boolean halted;
    public final int pageFault;
    public final SysCallRequest sysCall;

    public CPUResult(Interrupts interrupt, boolean halted, int pageFault, SysCallRequest sysCall) {
        this.interrupt = interrupt;
        this.halted = halted;
        this.pageFault = pageFault;
        this.sysCall = sysCall;
    }
}
