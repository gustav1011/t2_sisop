public class Sistema {

    public final HW hw;
    public final SO so;
    public final Programs progs;
    public final Disk disco;
    public final GerenteMemoria gm;
    public final GerenteProcessos gp;
    public final MMU mmu;
    public final Escalonador escalonador;
    private final Shell shell;

    public Sistema(int tamMem, int tamPag, int quantum) {
        hw = new HW(tamMem); // memoria do HW tem tamMem palavras
        so = new SO(hw);
        hw.cpu.setUtilities(so.utils); // permite cpu fazer dump de memoria ao avancar
        progs = new Programs();
        disco = new Disk();
        gm = new GerenteMemoria(hw, tamPag);
        gp = new GerenteProcessos(gm, hw, progs, disco);
        mmu = new MMU(hw, gm);
        hw.cpu.setMMU(mmu);
        escalonador = new Escalonador(gp, hw, quantum, gm, mmu, disco);
        shell = new Shell(this);
    }

    public void iniciar() {
        escalonador.iniciar();
        shell.run();
    }

    public void criaProcesso(String nome) {
        gp.criaProcesso(nome);
    }

    public void listaProcessos() {
        gp.listaProcessos();
    }

    public void mostraMemoria() {
        gm.mostraFrames();
    }

    public void desalocaProcesso(int pid) {
        gp.desalocaProcesso(pid);
    }

    public void encerrar() {
        escalonador.encerrar();
    }

    public static void main(String[] args) {
        Sistema s = new Sistema(1024, 16, 5);
        s.iniciar();
    }
}
