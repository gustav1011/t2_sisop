public class SysCallHandling {
    private final HW hw;

    public SysCallHandling(HW _hw) {
        hw = _hw;
    }

    public void stop() {
        System.out.println("                                               SYSCALL STOP");
    }

    public void handle() {
        int rw = hw.cpu.getRegister(8);
        int endereco = hw.cpu.getRegister(9);

        System.out.println("SYSCALL pars:  " + rw + " / " + endereco);

        if (rw == 1) {
            // leitura ...
        } else if (rw == 2) {
            System.out.println("OUT:   " + hw.mem.pos[endereco].p);
        } else {
            System.out.println("  PARAMETRO INVALIDO");
        }
    }
}

