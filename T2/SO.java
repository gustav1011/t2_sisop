public class SO {
    public final InterruptHandling ih;
    public final SysCallHandling sc;
    public final Utilities utils;

    public SO(HW hw) {
        ih = new InterruptHandling(hw);
        sc = new SysCallHandling(hw);
        hw.cpu.setAddressOfHandlers(ih, sc);
        utils = new Utilities(hw);
    }
}

