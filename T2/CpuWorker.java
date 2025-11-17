import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;

public class CpuWorker extends Thread {
    private static class ExecRequest {
        final GerenteProcessos.PCB pcb;
        final int quantum;
        final CompletableFuture<CPUResult> future;

        ExecRequest(GerenteProcessos.PCB pcb, int quantum, CompletableFuture<CPUResult> future) {
            this.pcb = pcb;
            this.quantum = quantum;
            this.future = future;
        }
    }

    private final BlockingQueue<ExecRequest> fila = new LinkedBlockingQueue<>();
    private final GerenteProcessos gp;
    private final CPU cpu;
    private volatile boolean ativo = true;

    public CpuWorker(GerenteProcessos gp, CPU cpu) {
        this.gp = gp;
        this.cpu = cpu;
        setName("CPU-Worker");
    }

    public CompletableFuture<CPUResult> executar(GerenteProcessos.PCB pcb, int quantum) {
        CompletableFuture<CPUResult> futuro = new CompletableFuture<>();
        fila.offer(new ExecRequest(pcb, quantum, futuro));
        return futuro;
    }

    public void encerrar() {
        ativo = false;
        fila.offer(new ExecRequest(null, 0, null));
    }

    @Override
    public void run() {
        while (ativo) {
            try {
                ExecRequest req = fila.take();
                if (!ativo || req.pcb == null) {
                    continue;
                }
                gp.restaurarContexto(req.pcb);
                cpu.setProcessoAtual(req.pcb);
                CPUResult resultado = cpu.run(req.quantum);
                gp.salvarContexto(req.pcb);
                cpu.setProcessoAtual(null);
                req.future.complete(resultado);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
