public class SO {
    public final InterruptHandling ih;
    public final SysCallHandling sc;
    public final Utilities utils;

    public SO(HW hw) {
        ih = new InterruptHandling(hw);
        syscall = new SysCallHandling(hw, esc, gp, dispositivo);
        hw.cpu.setAddressOfHandlers(ih, sc);
        utils = new Utilities(hw);
    }
}
