public class SO {
    public final InterruptHandling ih;
    public final SysCallHandling sc;
    public final Utilities utils;

    public SO(HW hw, SysCallHandling syscall, GerenteProcessos dispositivo, DispositivoIO gp) {
        ih = new InterruptHandling(hw);
        sc = new SysCallHandling(hw, dispositivo, gp);
        hw.cpu.setAddressOfHandlers(ih, sc);
        utils = new Utilities(hw);
    }
}
