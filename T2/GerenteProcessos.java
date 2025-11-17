import java.util.Queue;

public class GerenteProcessos {

    public static class PCB {
        public final int id;
        public final int[] registradores;
        public final PageTableEntry[] tabelaPaginas;
        public final String nomePrograma;
        public final int tamanhoPrograma;
        public ProcessState estado;
        public int pc;

        public PCB(int id, String nomePrograma, PageTableEntry[] tabelaPaginas, int tamanhoPrograma) {
            this.id = id;
            this.nomePrograma = nomePrograma;
            this.tabelaPaginas = tabelaPaginas;
            this.tamanhoPrograma = tamanhoPrograma;
            this.pc = 0;
            this.registradores = new int[10];
            this.estado = ProcessState.READY;
        }
    }

    private int proximoId = 1;
    private final Queue<PCB> filaProntos = new java.util.concurrent.ConcurrentLinkedQueue<>();
    private final Queue<PCB> filaBloqueados = new java.util.concurrent.ConcurrentLinkedQueue<>();
    private final java.util.Map<Integer, PCB> todos = new java.util.concurrent.ConcurrentHashMap<>();
    private PCB rodando;

    private final GerenteMemoria gm;
    private final HW hw;
    private final Programs progs;
    private final Disk disco;

    public GerenteProcessos(GerenteMemoria gm, HW hw, Programs progs, Disk disco) {
        this.gm = gm;
        this.hw = hw;
        this.progs = progs;
        this.disco = disco;
    }

    public PCB criaProcesso(String nomePrograma) {
        Word[] programa = progs.retrieveProgram(nomePrograma);
        if (programa == null) {
            System.out.println("Programa não encontrado: " + nomePrograma);
            return null;
        }

        int id = proximoId++;
        PageTableEntry[] tabelaPaginas = gm.criaTabelaPaginas(programa.length);
        PCB pcb = new PCB(id, nomePrograma, tabelaPaginas, programa.length);

        armazenarPaginasNoDisco(pcb, programa);
        carregarPrimeiraPagina(pcb);

        filaProntos.add(pcb);
        todos.put(pcb.id, pcb);

        System.out.println("Processo criado: id=" + pcb.id + " (" + nomePrograma + ")");
        return pcb;
    }

    private void armazenarPaginasNoDisco(PCB pcb, Word[] programa) {
        int tamPag = gm.getTamPag();
        Word[] pagina = new Word[tamPag];
        int paginaAtual = 0;
        int indiceNaPagina = 0;

        for (int i = 0; i < programa.length; i++) {
            pagina[indiceNaPagina++] = new Word(programa[i].opc, programa[i].ra, programa[i].rb, programa[i].p);
            if (indiceNaPagina == tamPag) {
                salvarPagina(pcb, paginaAtual++, pagina);
                pagina = new Word[tamPag];
                indiceNaPagina = 0;
            }
        }
        if (indiceNaPagina > 0) {
            while (indiceNaPagina < tamPag) {
                pagina[indiceNaPagina++] = new Word(Opcode.DATA, -1, -1, 0);
            }
            salvarPagina(pcb, paginaAtual, pagina);
        }
    }

    private void salvarPagina(PCB pcb, int numeroPagina, Word[] pagina) {
        int bloco = disco.storePage(pagina);
        pcb.tabelaPaginas[numeroPagina].diskBlock = bloco;
    }

    private void carregarPrimeiraPagina(PCB pcb) {
        if (pcb.tabelaPaginas.length == 0)
            return;
        PageTableEntry entrada = pcb.tabelaPaginas[0];
        Word[] conteudo = disco.readPage(entrada.diskBlock);
        int frame = gm.reservarFrame(pcb.id, entrada.pageNumber);
        if (frame < 0) {
            System.out.println("Sem frames livres para primeira página do processo " + pcb.id);
            return;
        }
        gm.escreveFrame(frame, conteudo);
        entrada.frameNumber = frame;
        entrada.present = true;
    }

    public PCB getProcesso(int id) {
        return todos.get(id);
    }

    public void desalocaProcesso(int id) {
        PCB alvo = todos.remove(id);
        if (alvo == null) {
            System.out.println("Processo " + id + " não encontrado.");
            return;
        }

        liberarPaginas(alvo);
        filaProntos.remove(alvo);
        filaBloqueados.remove(alvo);
        if (rodando == alvo) {
            rodando = null;
        }

        System.out.println("Processo " + id + " desalocado.");
    }

    private void liberarPaginas(PCB pcb) {
        for (PageTableEntry entrada : pcb.tabelaPaginas) {
            if (entrada.present && entrada.frameNumber >= 0) {
                gm.liberarFrame(entrada.frameNumber);
            }
            entrada.present = false;
            entrada.frameNumber = -1;
        }
    }

    public void listaProcessos() {
        System.out.println("----- Lista de Processos -----");
        for (PCB p : todos.values()) {
            System.out.println("ID: " + p.id + " | Nome: " + p.nomePrograma + " | Estado: " + p.estado);
        }
    }

    public boolean haProcessosProntos() {
        return !filaProntos.isEmpty();
    }

    public PCB proximoProcesso() {
        return filaProntos.poll();
    }

    public void refileiraProcesso(PCB pcb) {
        filaProntos.add(pcb);
        pcb.estado = ProcessState.READY;
    }

    public void bloquearProcesso(PCB pcb) {
        pcb.estado = ProcessState.BLOCKED;
        filaBloqueados.add(pcb);
    }

    public void desbloquearProcesso(PCB pcb) {
        filaBloqueados.remove(pcb);
        pcb.estado = ProcessState.READY;
        filaProntos.add(pcb);
    }

    public void setRodando(PCB pcb) {
        rodando = pcb;
        if (pcb != null) {
            pcb.estado = ProcessState.RUNNING;
        }
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
