public class SO {

    public final HW hw;
    public final GerenteProcessos gp;
    public final GerenteMemoria gm;
    public final Escalonador esc;
    public final DispositivoIO dispositivo;
    public final SysCallHandling syscall;
    public final InterruptHandling ih;
    public final Utilities utils;
    public final Disco disco;

    public SO(HW hw) {
        this.hw = hw;

        this.gm = hw.gm;
        this.gp = hw.gp;

        this.disco = new Disco();

        this.utils = new Utilities();

        this.ih = new InterruptHandling(hw, gp, esc, gm, dispositivo, disco);

        this.esc = new Escalonador(gp, hw, 5);

        this.dispositivo = new DispositivoIO(hw, gp, esc, disco);

        this.syscall = new SysCallHandling(
                hw,
                gp,
                dispositivo,
                disco,
                gm,
                esc);

        hw.cpu.setUtilities(utils);
        hw.cpu.setInterruptHandler(ih);
        hw.cpu.setSysCallHandler(syscall);

        System.out.println("[SO] Inicialização completa.");
    }

    public void iniciarThreads() {

        Thread tCPU = new Thread(hw.cpu, "CPU");
        tCPU.start();

        Thread tIO = new Thread(dispositivo, "DispositivoIO");
        tIO.start();

        Thread tClock = new Thread(new ThreadHardware(hw, esc, 0), "Clock");
        tClock.start();

        System.out.println("[SO] Threads iniciadas (CPU + IO + Clock).");
    }
}
