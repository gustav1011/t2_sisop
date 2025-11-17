public class SysCallHandling {
    private final HW hw;

    public SysCallHandling(HW _hw) {
        hw = _hw;
    }

    public void stop() {
        System.out.println("                                               SYSCALL STOP");
    }

    public SysCallRequest buildRequest(GerenteProcessos.PCB pcb) {
        int rw = hw.cpu.getRegister(8);
        int endereco = hw.cpu.getRegister(9);

        System.out.println("SYSCALL: processo " + pcb.id + " rw=" + rw + " endereco=" + endereco);

        if (rw == 1) {
            return new SysCallRequest(pcb, SysCallRequest.Type.READ, endereco, 0);
        } else if (rw == 2) {
            return new SysCallRequest(pcb, SysCallRequest.Type.WRITE, endereco, 0);
        } else {
            return new SysCallRequest(pcb, SysCallRequest.Type.INVALID, endereco, 0);
        }
    }
}
