import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DispositivoIO implements Runnable {

    private final BlockingQueue<PedidoIO> fila = new LinkedBlockingQueue<>();
    private final HW hw;
    private final GerenteProcessos gp;
    private final Escalonador esc;
    private final int tempoIOms;
    private volatile boolean running = true;

    public DispositivoIO(HW hw, GerenteProcessos gp, Escalonador esc, int tempoIOms) {
        this.hw = hw;
        this.gp = gp;
        this.esc = esc;
        this.tempoIOms = tempoIOms;
    }

    public void submit(PedidoIO pedido) {
        try {
            fila.put(pedido);
            System.out.println("[DispositivoIO] Pedido enfileirado: " + pedido);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("[DispositivoIO] submit interrompido");
        }
    }

    public void shutdown() {
        running = false;
        Thread.currentThread().interrupt();
    }

    @Override
    public void run() {
        System.out.println("[DispositivoIO] Iniciando dispositivo (tempoIO=" + tempoIOms + " ms)");
        while (running) {
            try {
                PedidoIO p = fila.take(); // bloqueia até ter pedido
                System.out.println("[DispositivoIO] Processando " + p);

                try {
                    Thread.sleep(tempoIOms);
                } catch (InterruptedException ie) {
                    if (!running)
                        break;
                    Thread.currentThread().interrupt();
                }

                // DMA: escreve/ler diretamente na memória física
                if (p.rw == PedidoIO.IN) {
                    int valorLido = (int) (Math.random() * 100); // simulação
                    hw.mem.pos[p.endereco].opc = Opcode.DATA;
                    hw.mem.pos[p.endereco].p = valorLido;
                    System.out.println("[DispositivoIO] IN concluído: pid=" + p.pid + " endereco=" + p.endereco
                            + " valor=" + valorLido);
                } else if (p.rw == PedidoIO.OUT) {
                    int valor = hw.mem.pos[p.endereco].p;
                    System.out.println(
                            "[DispositivoIO] OUT (pid=" + p.pid + "): endereco=" + p.endereco + " valor=" + valor);
                } else {
                    System.out.println("[DispositivoIO] Pedido inválido: " + p);
                }

                // 1) desbloqueia processo no Gerente de Processos
                gp.desbloquearProcessoById(p.pid);

                // 2) notifica o escalonador sobre a interrupção de IO
                esc.interrupcaoIO(p.pid);

            } catch (InterruptedException e) {
                if (!running)
                    break;
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                System.out.println("[DispositivoIO] Erro: " + e.getMessage());
                e.printStackTrace();
            }
        }
        System.out.println("[DispositivoIO] Finalizando dispositivo.");
    }
}
