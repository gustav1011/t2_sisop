import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

public class ConsoleDevice extends Thread {
    private final BlockingQueue<SysCallRequest> fila = new LinkedBlockingQueue<>();
    private final MMU mmu;
    private final Escalonador escalonador;
    private volatile boolean ativo = true;

    public ConsoleDevice(MMU mmu, Escalonador escalonador) {
        this.mmu = mmu;
        this.escalonador = escalonador;
        setName("ConsoleDevice");
    }

    public void solicitar(SysCallRequest req) {
        fila.offer(req);
    }

    public void encerrar() {
        ativo = false;
        fila.offer(new SysCallRequest(null, SysCallRequest.Type.INVALID, -1, 0));
    }

    @Override
    public void run() {
        while (ativo) {
            try {
                SysCallRequest req = fila.take();
                if (!ativo || req == null || req.type == SysCallRequest.Type.INVALID) {
                    continue;
                }
                processa(req);
                escalonador.notificaIOConclusao(req);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void processa(SysCallRequest req) {
        try {
            switch (req.type) {
                case WRITE:
                    int valor = mmu.readData(req.processo, req.address);
                    System.out.println("[CONSOLE] OUT P" + req.processo.id + " => " + valor);
                    break;
                case READ:
                    int entrada = ThreadLocalRandom.current().nextInt(1, 100);
                    mmu.writeData(req.processo, req.address, entrada);
                    System.out.println("[CONSOLE] IN P" + req.processo.id + " <= " + entrada);
                    break;
                default:
                    break;
            }
        } catch (PageFaultException e) {
            System.out.println("[CONSOLE] Page fault ao acessar endereço para IO do processo " + req.processo.id);
        }
    }
}

