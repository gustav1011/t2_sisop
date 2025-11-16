public class Sistema {

    public final HW hw;
    public final SO so;
    public final Programs progs;

    public Sistema(HW hw, SO so, Programs progs) {
        this.hw = hw;
        this.so = so;
        this.progs = progs;
        hw.cpu.setUtilities(so.utils);
    }

    public void run() {

    }

    public static void main(String[] args) {
        System.out.println("--- INICIANDO SISTEMA (Modo T2a) ---");

        int tamMemoria = 1024;
        int tamPagina = 16;
        int quantum = 5;

        HW hw = new HW(1024, quantum);
        Programs progs = new Programs();

        GerenteMemoria gm = new GerenteMemoria(hw, tamPagina);
        GerenteProcessos gp = new GerenteProcessos(hw, progs, gm);
        Escalonador esc = new Escalonador(gp, hw, quantum);

        DispositivoIO dispositivo = new DispositivoIO(hw, gp, esc, 500);
        Thread tDisp = new Thread(dispositivo, "DispositivoIO");
        tDisp.start();

        SysCallHandling sys = new SysCallHandling(hw, gp, dispositivo, null, gm, esc);
        SO so = new SO(hw);

        hw.cpu.setAddressOfHandlers(so.ih, sys);

        Sistema s = new Sistema(hw, so, progs);

        Thread tHardware = new Thread(new ThreadHardware(hw, esc, quantum), "ThreadHardware");

        Thread tShell = new Thread(new ThreadShell(s, gp, gm, esc), "ThreadShell");
        tShell.start();

        System.out.println("[Main] Sistema rodando. Use o Shell para interagir.");
    }
}