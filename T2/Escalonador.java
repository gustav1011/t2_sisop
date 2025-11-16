public class Escalonador {

    private final GerenteProcessos gp;
    private final HW hw;
    private final int delta; // quantum em ticks
    private GerenteProcessos.PCB processoAtual = null;
    private int quantumRestante = 0;

    public Escalonador(GerenteProcessos gp, HW hw, int delta) {
        this.gp = gp;
        this.hw = hw;
        this.delta = delta;
    }

    /**
     * Chamado pelo clock thread (ThreadHardware) periodicamente.
     * Executa UMA instrução do processo atual e trata preempção / bloqueio / fim.
     */
    public synchronized void interrupcaoClock() {
        // Se não há processo atual, tenta buscar próximo
        if (processoAtual == null) {
            if (!gp.haProcessosProntos()) {
                return;
            }
            processoAtual = gp.proximoProcesso();
            if (processoAtual == null)
                return;
            gp.setRodando(processoAtual);
            processoAtual.estado = GerenteProcessos.PCB.EstadoProcesso.EXECUTANDO;
            System.out.println(
                    "\n[Escalonador] Escalonando pid=" + processoAtual.id + " (" + processoAtual.nomePrograma + ")");
            gp.restaurarContexto(processoAtual);
            quantumRestante = delta;
        }

        // Executa UMA instrução (step). step() pode lançar SyscallBlockedException.
        try {
            hw.cpu.step();
        } catch (SyscallBlockedException sbe) {
            System.out.println("[Escalonador] Processo " + processoAtual.id + " bloqueado por SYSCALL.");
            gp.salvarContexto(processoAtual);
            gp.bloquearProcesso(processoAtual);
            gp.limparRodando();
            processoAtual = null;
            return;
        } catch (Exception e) {
            System.out.println("[Escalonador] Erro ao executar step() do CPU: " + e.getMessage());
            e.printStackTrace();
            // para segurança, desaloca processo (ou trate conforme desejar)
            if (processoAtual != null) {
                processoAtual.estado = GerenteProcessos.PCB.EstadoProcesso.FINALIZADO;
                gp.desalocaProcesso(processoAtual.id);
                gp.limparRodando();
                processoAtual = null;
            }
            return;
        }

        // Se o processo terminou (STOP -> cpu.isHalted() == true)
        if (hw.cpu.isHalted()) {
            System.out.println("[Escalonador] Processo " + processoAtual.id + " finalizado.");
            processoAtual.estado = GerenteProcessos.PCB.EstadoProcesso.FINALIZADO;
            gp.desalocaProcesso(processoAtual.id);
            gp.limparRodando();
            processoAtual = null;
            return;
        }

        // decrementa quantum e preempção
        quantumRestante--;
        if (quantumRestante <= 0) {
            // salva contexto, refileira
            gp.salvarContexto(processoAtual);
            processoAtual.estado = GerenteProcessos.PCB.EstadoProcesso.PRONTO;
            gp.refileiraProcesso(processoAtual);
            gp.limparRodando();
            processoAtual = null;
        }
    }

    /**
     * Chamado pelo dispositivo quando termina um pedido IO.
     * Aqui apenas logamos — o dispositivo já chamou
     * gp.desbloquearProcessoById(pid).
     * Opcionalmente aqui podemos forçar preempção.
     */
    public synchronized void interrupcaoIO(int pid) {
        System.out.println("[Escalonador] Interrupcao IO recebida para pid=" + pid
                + ". Processo deve estar em PRONTO (se desbloqueado).");
        // Opcional: forçar preempção do processo atual para permitir escalonar o
        // recém-desbloqueado.
        // Implementação simples (não forçar por enquanto).
    }

    // Mantive execAll por compatibilidade (modo bloqueante antigo)
    public void execAll() {
        System.out.println("\n=== INICIANDO ESCALONAMENTO (modo bloqueante) ===");
        
        while (gp.haProcessosProntos()) {
            GerenteProcessos.PCB pcb = gp.proximoProcesso();
            if (pcb == null) break;

            gp.setRodando(pcb);
            pcb.estado = GerenteProcessos.PCB.EstadoProcesso.EXECUTANDO;
            System.out.println("\n[CPU] Rodando processo " + pcb.id + " (" + pcb.nomePrograma + ")");
            
            gp.restaurarContexto(pcb);

            boolean terminou = false;
            boolean foiBloqueado = false;

            try {
                hw.cpu.run(); // Executa até STOP ou gerar Exceção (Syscall)
                terminou = true; // Se chegou aqui, é porque deu STOP (fim normal)
            } catch (SyscallBlockedException sbe) {
                // --- MUDANÇA PRINCIPAL AQUI ---
                System.out.println("[Escalonador] Processo " + pcb.id + " solicitou I/O e foi BLOQUEADO.");
                
                // 1. Salva onde parou
                gp.salvarContexto(pcb);
                
                // 2. Move para a lista de bloqueados (NÃO volta para fila de prontos agora)
                gp.bloquearProcesso(pcb);
                
                foiBloqueado = true;
                
            } catch (Exception e) {
                System.out.println("Erro FATAL na execução do processo " + pcb.id + ": " + e.getMessage());
                terminou = true; // Considera finalizado por erro
            }

            // Lógica de Decisão Pós-Execução
            if (terminou) {
                System.out.println("Processo " + pcb.id + " finalizado.");
                pcb.estado = GerenteProcessos.PCB.EstadoProcesso.FINALIZADO;
                gp.desalocaProcesso(pcb.id);
            } 
            // Se não terminou e NÃO foi bloqueado (ex: preempção por tempo, se houvesse), volta pra fila
            else if (!foiBloqueado) {
                gp.salvarContexto(pcb);
                pcb.estado = GerenteProcessos.PCB.EstadoProcesso.PRONTO;
                gp.refileiraProcesso(pcb);
            }

            gp.limparRodando();
        }
        System.out.println("\n=== FILA DE PRONTOS VAZIA (Verifique se há processos Bloqueados no Log) ===");
    }
}
