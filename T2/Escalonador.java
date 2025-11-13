public class Escalonador {

    private final GerenteProcessos gp;
    private final HW hw;
    private final int delta; // ainda não utilizado

    public Escalonador(GerenteProcessos gp, HW hw, int delta) {
        this.gp = gp;
        this.hw = hw;
        this.delta = delta;
    }

    public void execAll() {
        System.out.println("\n=== INICIANDO ESCALONAMENTO ROUND-ROBIN ===");

        while (gp.haProcessosProntos()) {
            GerenteProcessos.PCB pcb = gp.proximoProcesso();
            if (pcb == null) {
                break;
            }
            gp.setRodando(pcb);
            pcb.estado = "Executando";

            System.out.println("\n[CPU] Rodando processo " + pcb.id + " (" + pcb.nomePrograma + ")");
            gp.restaurarContexto(pcb);

            boolean terminou = false;
            try {
                hw.cpu.run();
                terminou = true;
            } catch (Exception e) {
                System.out.println("Erro na execução do processo " + pcb.id);
            }

            if (terminou) {
                System.out.println("Processo " + pcb.id + " finalizado.");
                pcb.estado = "Finalizado";
                gp.desalocaProcesso(pcb.id);
            } else {
                gp.salvarContexto(pcb);
                pcb.estado = "Pronto";
                gp.refileiraProcesso(pcb);
            }

            gp.limparRodando();
        }

        System.out.println("\n=== TODOS OS PROCESSOS FINALIZADOS ===");
    }
}

