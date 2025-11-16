public class HW {

    public final Memory mem;
    public final CPU cpu;
    public final GerenteMemoria gm;
    public final GerenteProcessos gp;
    public final Programs progs;

    public final int tamMem;
    public final int tamPag;
    public Object memory;

    public HW(int tamMem, int tamPag) {
        this.tamMem = tamMem;
        this.tamPag = tamPag;

        this.mem = new Memory(tamMem);

        this.progs = new Programs();

        this.gm = new GerenteMemoria(tamMem, tamPag);

        this.gp = new GerenteProcessos(this, gm, progs);

        this.cpu = new CPU(this, gp);

        System.out.println("[HW] Inicializado: Memória=" + tamMem + " palavras, página=" + tamPag + " palavras.");
    }

    public int getTamPag() {
        return tamPag;
    }
}
