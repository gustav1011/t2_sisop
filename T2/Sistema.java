public class Sistema {

    public final HW hw;
    public final SO so;
    public final Programs progs;

    // Construtor ajustado para receber os componentes já criados
    // Isso evita que existam duas memórias ou duas CPUs diferentes
    public Sistema(HW hw, SO so, Programs progs) {
        this.hw = hw;
        this.so = so;
        this.progs = progs;
        hw.cpu.setUtilities(so.utils);
    }

    // Mantido para compatibilidade, mas não será muito usado na VM
    public void run() {
        // Exemplo: Carregar programas na memória (apenas para teste sem VM)
        // Com VM, usaremos o Shell -> CriaProcesso
    }

    public static void main(String[] args) {
        System.out.println("--- INICIANDO SISTEMA (Modo T2a) ---");

        // 1) PARÂMETROS DO SISTEMA
        int tamMemoria = 1024;
        int tamPagina = 16; // Paging (Fase 1/T2a)
        int quantum = 5;

        // 2) CRIAÇÃO DO HARDWARE (ÚNICO)
        // Certifique-se que HW.java tem o construtor (int, int) para tamPag
        HW hw = new HW(1024); 
        Programs progs = new Programs();

        // 3) CRIAÇÃO DOS GERENTES (ÚNICOS)
        // Todos usam o MESMO 'hw'
        GerenteMemoria gm = new GerenteMemoria(hw, tamPagina);
        GerenteProcessos gp = new GerenteProcessos(hw, progs, gm);
        Escalonador esc = new Escalonador(gp, hw, quantum);

        // 4) DISPOSITIVOS DE I/O (Threads)
        DispositivoIO dispositivo = new DispositivoIO(hw, gp, esc, 500); 
        Thread tDisp = new Thread(dispositivo, "DispositivoIO");
        tDisp.start();
        
        // (Futuro: DispositivoSwap virá aqui)

        // 5) HANDLERS
        // 'sys' é o handler de SYSCALL
        SysCallHandling sys = new SysCallHandling(hw, gp, dispositivo);
        // 'so' é um container para o InterruptHandling (ih)
        SO so = new SO(hw, sys, gp, dispositivo);
        
        // Conecta os handlers na CPU
        hw.cpu.setAddressOfHandlers(so.ih, sys); 

        // 6) CRIA O 'SISTEMA' (só para passar ao Shell)
        // Este construtor NÃO deve criar um new HW()
        Sistema s = new Sistema(hw, so, progs); 

        // 7) THREADS DE CONTROLE
        // Clock (se estiver usando)
        Thread tHardware = new Thread(new ThreadHardware(hw, esc), "ThreadHardware");
        //tHardware.start(); // <-- Se quiser que rode automático

        // Shell
        Thread tShell = new Thread(new ThreadShell(s, gp, gm, esc), "ThreadShell");
        tShell.start();

        System.out.println("[Main] Sistema rodando. Use o Shell para interagir.");
    }
}