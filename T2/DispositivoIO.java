import java.util.LinkedList;
import java.util.Queue;

public class DispositivoIO implements Runnable {

    public static class IORequest {
        public final int pid;
        public final int address;

        public IORequest(int pid, int address) {
            this.pid = pid;
            this.address = address;
        }
    }

    private final Queue<IORequest> fila = new LinkedList<>();

    private final HW hw;
    private final GerenteProcessos gp;
    private final Escalonador esc;

    private final int tempoIOms;

    public DispositivoIO(HW hw, GerenteProcessos gp, Escalonador esc, Disco disco) {
        this.hw = hw;
        this.gp = gp;
        this.esc = esc;
        this.tempoIOms = disco;
    }

    public synchronized void requisitarIO(int pid, int address) {
        fila.add(new IORequest(pid, address));
        System.out.println("[IO] Requisição recebida do processo " + pid);
        notifyAll();
    }

    @Override
    public void run() {
        System.out.println("[IO] Thread do dispositivo iniciada.");

        while (true) {
            IORequest req = null;

            synchronized (this) {
                while (fila.isEmpty()) {
                    try {
                        wait();
                    } catch (InterruptedException ignored) {
                    }
                }
                req = fila.poll();
            }

            if (req != null) {
                System.out.println("[IO] Executando I/O para processo " + req.pid +
                        " (end=" + req.address + ")");

                try {
                    Thread.sleep(tempoIOms);
                } catch (InterruptedException ignored) {
                }

                System.out.println("[IO] I/O concluída para processo " + req.pid);

                gp.desbloquearProcessoById(req.pid);

                esc.notificarIOConcluido();

                hw.cpu.interruptIO(req.pid);
            }
        }
    }
}
