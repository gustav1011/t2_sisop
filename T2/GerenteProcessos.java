import java.util.*;

public class GerenteProcessos {

    private int nextPID = 1;

    private final GerenteMemoria gm;
    private final Disco disco;

    private final Map<Integer, PCB> processos = new HashMap<>();

    private final Queue<PCB> prontos = new LinkedList<>();

    public GerenteProcessos(GerenteMemoria gm, Disco disco) {
        this.gm = gm;
        this.disco = disco;
    }

    public GerenteProcessos(HW hw, Programs progs, GerenteMemoria gm2) {
        // TODO Auto-generated constructor stub
    }

    public PCB criarProcesso(Word[] programa) throws PageFaultException {

        int pid = nextPID++;

        PCB pcb = new PCB(pid);

        int paginas = (int) Math.ceil((double) programa.length / gm.getTamPag());

        pcb.pageTable = new PageTableEntry[paginas];

        for (int p = 0; p < paginas; p++) {
            pcb.pageTable[p] = new PageTableEntry();
        }

        gm.carregarPagina(programa, 0, pcb.pageTable[0]);

        pcb.estado = Estado.READY;
        pcb.program = programa;

        processos.put(pid, pcb);
        prontos.add(pcb);

        System.out.println("[GP] Processo PID=" + pid + " criado com " + paginas + " páginas.");

        return pcb;
    }

    public PCB obterProximoProcesso() {
        if (prontos.isEmpty())
            return null;
        PCB pcb = prontos.poll();
        pcb.estado = Estado.RUNNING;
        return pcb;
    }

    public void devolverParaProntos(PCB pcb) {
        if (pcb != null && pcb.estado != Estado.FINISHED) {
            pcb.estado = Estado.READY;
            prontos.add(pcb);
        }
    }

    public void finalizarProcesso(PCB pcb) {
        pcb.estado = Estado.FINISHED;
        processos.remove(pcb.pid);
        gm.desalocarProcesso(pcb.pageTable);
        System.out.println("[GP] Processo " + pcb.pid + " finalizado.");
    }

    public void tratarPageFault(PCB pcb, int paginaFaltante) throws PageFaultException {
        System.out.println("[GP] Tratando page-fault do processo " + pcb.pid + " na página " + paginaFaltante);

        gm.carregarPagina(pcb.program, paginaFaltante, pcb.pageTable[paginaFaltante]);
    }

    public boolean temProcessosProntos() {
        return !prontos.isEmpty();
    }

    public PCB getProcesso(int pid) {
        return processos.get(pid);
    }

    public int totalProcessos() {
        return processos.size();
    }

    public class PCB {

        public Object tabelaPaginas;
    }

    public GerenteProcessos.PCB getRodando() {
        throw new UnsupportedOperationException("Unimplemented method 'getRodando'");
    }
}
