import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class GerenteProcessos {

    public static class PCB {
        public final int id;
        public final int[] registradores;
        public final int[] tabelaPaginas;
        public final String nomePrograma;
        public String estado;
        public int pc;

        public PCB(int id, String nomePrograma, int[] tabelaPaginas) {
            this.id = id;
            this.nomePrograma = nomePrograma;
            this.tabelaPaginas = tabelaPaginas;
            this.pc = 0;
            this.registradores = new int[10];
            this.estado = "Pronto";
        }
    }

    private int proximoId = 1;
    private final Queue<PCB> filaProntos = new LinkedList<>();
    private final List<PCB> todos = new ArrayList<>();
    private PCB rodando = null;

    private final GerenteMemoria gm;
    private final HW hw;
    private final Programs progs;

    public GerenteProcessos(GerenteMemoria gm, HW hw, Programs progs) {
        this.gm = gm;
        this.hw = hw;
        this.progs = progs;
    }

    public boolean criaProcesso(String nomePrograma) {
        Word[] programa = progs.retrieveProgram(nomePrograma);
        if (programa == null) {
            System.out.println("Programa não encontrado: " + nomePrograma);
            return false;
        }

        int[] tabelaPaginas = gm.aloca(programa.length);
        if (tabelaPaginas == null) {
            System.out.println("Sem memória suficiente para o processo " + nomePrograma);
            return false;
        }

        carregarPrograma(programa, tabelaPaginas);

        PCB pcb = new PCB(proximoId++, nomePrograma, tabelaPaginas);
        filaProntos.add(pcb);
        todos.add(pcb);

        System.out.println("Processo criado: id=" + pcb.id + " (" + nomePrograma + ")");
        return true;
    }

    private void carregarPrograma(Word[] programa, int[] tabelaPaginas) {
        Word[] mem = hw.mem.pos;
        int tamPag = gm.getTamPag();

        for (int i = 0; i < programa.length; i++) {
            int pagina = i / tamPag;
            int desloc = i % tamPag;
            int frame = tabelaPaginas[pagina];
            int enderecoFisico = frame * tamPag + desloc;

            mem[enderecoFisico].opc = programa[i].opc;
            mem[enderecoFisico].ra = programa[i].ra;
            mem[enderecoFisico].rb = programa[i].rb;
            mem[enderecoFisico].p = programa[i].p;
        }
    }

    public void desalocaProcesso(int id) {
        PCB alvo = null;
        for (PCB p : todos) {
            if (p.id == id) {
                alvo = p;
            }
        }

        if (alvo == null) {
            System.out.println("Processo " + id + " não encontrado.");
            return;
        }

        gm.desaloca(alvo.tabelaPaginas);
        todos.remove(alvo);
        filaProntos.remove(alvo);
        if (rodando == alvo) {
            rodando = null;
        }

        System.out.println("Processo " + id + " desalocado.");
    }

    public void listaProcessos() {
        System.out.println("----- Lista de Processos -----");
        for (PCB p : todos) {
            System.out.println("ID: " + p.id + " | Nome: " + p.nomePrograma + " | Estado: " + p.estado);
        }
    }

    public void executaProcesso(int id) {
        for (PCB p : todos) {
            if (p.id == id) {
                rodando = p;
                p.estado = "Executando";
                System.out.println("Executando processo " + p.id + " (" + p.nomePrograma + ")");
                hw.cpu.setContext(0);
                hw.cpu.run();
                p.estado = "Finalizado";
                rodando = null;
                return;
            }
        }
        System.out.println("Processo com id " + id + " não encontrado.");
    }

    public boolean haProcessosProntos() {
        return !filaProntos.isEmpty();
    }

    public PCB proximoProcesso() {
        return filaProntos.poll();
    }

    public void refileiraProcesso(PCB pcb) {
        filaProntos.add(pcb);
    }

    public void setRodando(PCB pcb) {
        rodando = pcb;
    }

    public void limparRodando() {
        rodando = null;
    }

    public void salvarContexto(PCB pcb) {
        pcb.pc = hw.cpu.getPc();
        hw.cpu.copyRegistersTo(pcb.registradores);
        System.out.println("[Contexto salvo] Processo " + pcb.id + " (pc=" + pcb.pc + ")");
    }

    public void restaurarContexto(PCB pcb) {
        hw.cpu.setContext(pcb.pc);
        hw.cpu.restoreRegisters(pcb.registradores);
        hw.cpu.setStopped(false);
        System.out.println("[Contexto restaurado] Processo " + pcb.id + " (pc=" + pcb.pc + ")");
    }
}

