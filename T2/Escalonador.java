
public class Escalonador {

    private final GerenteProcessos gp;
    private final HW hw;
    private final int delta;

    private GerenteProcessos.PCB processoAtual = null;
    private int quantumRestante = 0;

    public Escalonador(GerenteProcessos gp, HW hw, int delta) {
        this.gp = gp;
        this.hw = hw;
        this.delta = delta;
    }

    public synchronized void interrupcaoClock() {
        if (processoAtual == null) {
            if (!gp.haProcessosProntos()) {
                return;
            }
            processoAtual = gp.proximoProcesso();
            if (processoAtual == null)
                return;

            gp.setRodando(processoAtual);
            processoAtual.estado = GerenteProcessos.PCB.EstadoProcesso.EXECUTANDO;
            System.out.println("\n[Escalonador] Escalonando processo " + processoAtual.id + " ("
                    + processoAtual.nomePrograma + ")");
            gp.restaurarContexto(processoAtual);
            quantumRestante = delta;
            hw.cpu.setStopped(false);
        }

        try {
            hw.cpu.step();
        } catch (SyscallBlockedException sbe) {
            System.out.println("[Escalonador] Processo " + processoAtual.id + " bloqueado por SYSCALL.");
            gp.salvarContexto(processoAtual);
            gp.bloquearProcesso(processoAtual);
            gp.limparRodando();
            processoAtual = null;
            return;
        } catch (PageFaultException pfe) {
            System.out.println("[Escalonador] PageFault no processo " + pfe.getPid() + " pagina " + pfe.getPage());
            try {
                tratarPageFault(pfe);
            } catch (Exception e) {
                System.out.println("[Escalonador] Erro ao tratar page fault: " + e.getMessage());
                e.printStackTrace();
            }
            processoAtual = null;
            return;
        } catch (Exception e) {
            System.out.println("[Escalonador] Erro ao executar step(): " + e.getMessage());
            e.printStackTrace();
            if (processoAtual != null) {
                gp.salvarContexto(processoAtual);
                processoAtual.estado = GerenteProcessos.PCB.EstadoProcesso.PRONTO;
                gp.refileiraProcesso(processoAtual);
                gp.limparRodando();
                processoAtual = null;
            }
            return;
        }

        if (processoAtual != null && processoAtual.estado == GerenteProcessos.PCB.EstadoProcesso.FINALIZADO) {
            System.out.println("[Escalonador] Processo " + processoAtual.id + " finalizou.");
            gp.desalocaProcesso(processoAtual.id);
            gp.limparRodando();
            processoAtual = null;
            return;
        }

        if (processoAtual != null) {
            quantumRestante--;
            if (quantumRestante <= 0) {
                System.out.println("[Escalonador] Quantum esgotado para processo " + processoAtual.id + ". Preempção.");
                gp.salvarContexto(processoAtual);
                processoAtual.estado = GerenteProcessos.PCB.EstadoProcesso.PRONTO;
                gp.refileiraProcesso(processoAtual);
                gp.limparRodando();
                processoAtual = null;
            }
        }
    }

    private void tratarPageFault(PageFaultException pfe) {
        int pid = pfe.getPid();
        int pagina = pfe.getPage();

        GerenteProcessos.PCB pcb = processoAtual;
        if (pcb == null || pcb.id != pid) {
            pcb = null;
            for (GerenteProcessos.PCB p : gp.getAllProcessos()) {
                if (p.id == pid) {
                    pcb = p;
                    break;
                }
            }
        }

        if (pcb == null) {
            System.out.println("[Escalonador] PCB do pagefault não encontrado: pid=" + pid);
            return;
        }

        gp.salvarContexto(pcb);
        gp.bloquearProcesso(pcb);
        gp.limparRodando();
        System.out.println("[Escalonador] Processo " + pid + " bloqueado por page-fault (page=" + pagina + ").");

        try {
            hw.gm.handlePageFaultRequest(pid, pagina, this); // <-- você deve implementar esse método no GerenteMemoria
        } catch (UnsupportedOperationException uo) {
            System.out.println("[Escalonador] handlePageFaultRequest não implementado no GerenteMemoria.");
        }
    }

    public synchronized void notificaPageLoaded(int pid, int pagina, int frame) {
        System.out.println(
                "[Escalonador] Notificação: página carregada pid=" + pid + " page=" + pagina + " frame=" + frame);
        gp.desbloquearProcessoById(pid);
    }

    public synchronized void interrupcaoIO(int pid) {
        System.out.println("[Escalonador] Interrupcao IO recebida para pid=" + pid
                + ". Processo será colocado em PRONTO se aplicável.");
    }

    public void execAll() {
        System.out.println("\n=== INICIANDO ESCALONAMENTO ROUND-ROBIN (modo bloqueante) ===");

        while (gp.haProcessosProntos()) {
            GerenteProcessos.PCB pcb = gp.proximoProcesso();
            if (pcb == null)
                break;
            gp.setRodando(pcb);
            pcb.estado = GerenteProcessos.PCB.EstadoProcesso.EXECUTANDO;
            System.out.println("\n[CPU] Rodando processo " + pcb.id + " (" + pcb.nomePrograma + ")");
            gp.restaurarContexto(pcb);

            boolean terminou = false;
            try {
                hw.cpu.run();
                terminou = true;
            } catch (Exception e) {
                System.out.println("Erro na execução do processo " + pcb.id + ": " + e.getMessage());
            }

            if (terminou) {
                System.out.println("Processo " + pcb.id + " finalizado.");
                pcb.estado = GerenteProcessos.PCB.EstadoProcesso.FINALIZADO;
                gp.desalocaProcesso(pcb.id);
            } else {
                gp.salvarContexto(pcb);
                pcb.estado = GerenteProcessos.PCB.EstadoProcesso.PRONTO;
                gp.refileiraProcesso(pcb);
            }

            gp.limparRodando();
        }

        System.out.println("\n=== TODOS OS PROCESSOS FINALIZADOS ===");
    }

    public void interromperQuantum() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'interromperQuantum'");
    }
}
