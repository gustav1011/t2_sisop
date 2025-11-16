import java.util.Arrays;

public class CPU implements Runnable {

    private final HW hw;
    private final GerenteProcessos gp;

    private InterruptHandling ih;
    private SysCallHandling sh;
    private Utilities utils;

    private int pc;
    private int[] registradores;
    private boolean stopped = true;
    private boolean rodando = true;

    public CPU(HW hw, GerenteProcessos gp) {
        this.hw = hw;
        this.gp = gp;
        this.registradores = new int[10];
        this.pc = 0;
    }

    // usado pelo SO para conectar handlers
    public void setAddressOfHandlers(InterruptHandling ih, SysCallHandling sh) {
        this.ih = ih;
        this.sh = sh;
    }

    public void setUtilities(Utilities u) {
        this.utils = u;
    }

    public void stop() {
        stopped = true;
    }

    public void setStopped(boolean b) {
        stopped = b;
    }

    public int getPc() {
        return pc;
    }

    public void setContext(int novoPC) {
        this.pc = novoPC;
        stopped = false;
    }

    public void copyRegistersTo(int[] destino) {
        System.arraycopy(registradores, 0, destino, 0, registradores.length);
    }

    public void restoreRegisters(int[] origem) {
        System.arraycopy(origem, 0, registradores, 0, origem.length);
    }

    // tradução de endereço lógico → físico
    private int traduz(int enderecoLogico, GerenteProcessos.PCB pcb) throws PageFaultException {

        int pagina = enderecoLogico / hw.gm.getTamPag();
        int desloc = enderecoLogico % hw.gm.getTamPag();

        if (pagina >= pcb.tabelaPaginas.length) {
            throw new RuntimeException("Acesso ilegal fora do programa");
        }

        PageTableEntry pte = pcb.tabelaPaginas[pagina];

        // PAGE FAULT
        if (!pte.present) {
            throw new PageFaultException(pcb.id, pagina);
        }

        return pte.frame * hw.gm.getTamPag() + desloc;
    }

    private int lerMemoria(int enderecoLogico, GerenteProcessos.PCB pcb) throws PageFaultException {
        int fisico = traduz(enderecoLogico, pcb);
        return hw.memory.mem[fisico].p;
    }

    private void escreverMemoria(int enderecoLogico, int valor, GerenteProcessos.PCB pcb) throws PageFaultException {
        int fisico = traduz(enderecoLogico, pcb);
        hw.memory.mem[fisico].p = valor;

        // marcar página como modificada
        int pagina = enderecoLogico / hw.gm.getTamPag();
        pcb.tabelaPaginas[pagina].dirty = true;
    }

    // decodificação e execução de instrução
    private void executarInstrucao(GerenteProcessos.PCB pcb) throws PageFaultException {

        int enderecoFisico = traduz(pc, pcb);
        Word instr = hw.memory.mem[enderecoFisico];

        switch (instr.opc) {

            case NOP:
                pc++;
                break;

            case ADD:
                registradores[instr.ra] = registradores[instr.rb] + instr.p;
                pc++;
                break;

            case SUB:
                registradores[instr.ra] = registradores[instr.rb] - instr.p;
                pc++;
                break;

            case JMP:
                pc = instr.p;
                break;

            case JNZ:
                if (registradores[instr.ra] != 0)
                    pc = instr.p;
                else
                    pc++;
                break;

            case SYS:
                sh.handleSyscall(instr.p, pcb);
                break;

            case STR: // MEM[regB + p] = regA
                escreverMemoria(registradores[instr.rb] + instr.p, registradores[instr.ra], pcb);
                pc++;
                break;

            case LOAD: // regA = MEM[regB + p]
                registradores[instr.ra] = lerMemoria(registradores[instr.rb] + instr.p, pcb);
                pc++;
                break;

            case STOP:
                pcb.estado = GerenteProcessos.PCB.EstadoProcesso.FINALIZADO;
                stopped = true;
                break;

            default:
                throw new RuntimeException("[CPU] opcode invalido: " + instr.opc);
        }
    }

    @Override
    public void run() {

        System.out.println("[CPU] Iniciada!");

        while (rodando) {

            if (stopped) {
                try {
                    Thread.sleep(1);
                } catch (Exception e) {
                }
                continue;
            }

            GerenteProcessos.PCB pcb = gp.getRodando();
            if (pcb == null) {
                try {
                    Thread.sleep(1);
                } catch (Exception e) {
                }
                continue;
            }

            try {
                executarInstrucao(pcb);
            } catch (Exception e) {
                System.out.println("[CPU] ERRO: " + e.getMessage());
                stopped = true;
            }

        }
    }

    public void setSysCallHandler(SysCallHandling syscall) {
        throw new UnsupportedOperationException("Unimplemented method 'setSysCallHandler'");
    }
}
