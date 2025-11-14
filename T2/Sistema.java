
public class Sistema {

    public final HW hw;
    public final SO so;
    public final Programs progs;

    public Sistema(int tamMem) {
        hw = new HW(tamMem); // memoria do HW tem tamMem palavras
        so = new SO(hw);
        hw.cpu.setUtilities(so.utils); // permite cpu fazer dump de memoria ao avancar
        progs = new Programs();
    }

    public void run() {
        so.utils.loadAndExec(progs.retrieveProgram("fatorialV2"));
        so.utils.loadAndExec(progs.retrieveProgram("fatorial"));
        // fibonacci10,
        // fibonacci10v2,
        // progMinimo,
        // fatorialWRITE, // saida
        // fibonacciREAD, // entrada
        // PB
        // PC, // bubble sort
    }

    public static void main(String[] args) {
        // 1) componentes básicos
        Sistema s = new Sistema(1024); // cria HW, SO, utils, programs
        GerenteMemoria gm = new GerenteMemoria(s.hw, 16);
        GerenteProcessos gp = new GerenteProcessos(s.hw);
        Escalonador esc = new Escalonador(gp, s.hw, 5); // quantum = 5 ticks

        // 2) dispositivo de I/O
        DispositivoIO dispositivo = new DispositivoIO(s.hw, gp, esc, 500); // 500ms por pedido
        Thread tDisp = new Thread(dispositivo, "DispositivoIO");
        tDisp.start();

        // 3) syscall handler assíncrono (usa o dispositivo)
        SysCallHandling sys = new SysCallHandling(s.hw, gp, dispositivo);

        // 4) registrar handlers (IH já existe em s.so, substituir o handler de syscall)
        s.hw.cpu.setAddressOfHandlers(s.so.ih, sys); // setAddressOfHandlers(InterruptHandling, SysCallHandling)

        // 5) thread do hardware (clock)
        Thread tHardware = new Thread(new ThreadHardware(s.hw, esc), "ThreadHardware");
        tHardware.start();

        // 6) shell (interface com usuário) - roda em thread
        GerenteMemoria gmRef = gm; // apenas para passar ao shell
        Thread tShell = new Thread(new ThreadShell(s, gp, gmRef, esc), "ThreadShell");
        tShell.start();

        System.out.println("[MainSistema] Sistema iniciado. Shell, Clock e Dispositivo rodando.");
    }

}
