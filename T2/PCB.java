import java.util.Arrays;

public class PCB {

    public final int pid;

    public enum Estado {
        NOVO, PRONTO, EXECUTANDO, BLOQUEADO, TERMINADO
    }

    public Estado estado;

    public int pc;

    public int[] registradores;

    public PageTableEntry[] pageTable;

    public int tamanho;

    public boolean esperandoIO = false;

    public int quantumRestante;

    public int logicalAddressAposPageFault = -1;

    public boolean haltedByPageFault = false;

    public PCB(int pid, int tamProcesso, int tamPaginas, int quantumInicial) {
        this.pid = pid;
        this.estado = Estado.NOVO;

        this.pc = 0;
        this.registradores = new int[8];

        this.tamanho = tamProcesso;

        int numPaginas = (int) Math.ceil((double) tamProcesso / tamPaginas);
        this.pageTable = new PageTableEntry[numPaginas];
        for (int i = 0; i < numPaginas; i++) {
            this.pageTable[i] = new PageTableEntry();
        }

        this.quantumRestante = quantumInicial;
    }

    public void setEstado(Estado e) {
        this.estado = e;
    }

    public void salvarContexto(int pc, int[] regs) {
        this.pc = pc;
        this.registradores = Arrays.copyOf(regs, regs.length);
    }

    public void restaurarContexto(int[] destinoRegs) {
        System.arraycopy(registradores, 0, destinoRegs, 0, registradores.length);
    }

    public int getNumPaginas() {
        return pageTable.length;
    }

    public PageTableEntry getEntradaPagina(int pagina) {
        return pageTable[pagina];
    }

    public boolean terminou() {
        return estado == Estado.TERMINADO;
    }

    @Override
    public String toString() {
        return "PCB{ pid=" + pid +
                ", estado=" + estado +
                ", pc=" + pc +
                ", paginas=" + pageTable.length +
                " }";
    }
}
