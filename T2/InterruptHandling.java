public class InterruptHandling {

    private final HW hw;
    private final GerenteProcessos gp;
    private final Escalonador esc;
    private final GerenteMemoria gm;
    private final DispositivoIO dispositivo;
    private final Disco disco;

    public InterruptHandling(HW hw, GerenteProcessos gp, Escalonador esc,
            GerenteMemoria gm, DispositivoIO dispositivo, Disco disco) {

        this.hw = hw;
        this.gp = gp;
        this.esc = esc;
        this.gm = gm;
        this.dispositivo = dispositivo;
        this.disco = disco;
    }

    public synchronized void handleIOComplete(PedidoIO pedido) {
        System.out.println("[INT] Fim de IO comum. Processo " + pedido.pid);

        gp.desbloquearProcessoById(pedido.pid);

        esc.interromperQuantum();
    }

    public synchronized void handlePageLoadComplete(PedidoIO pedido) {
        System.out.println("[INT] Página carregada do DISCO: pid=" + pedido.pid +
                " page=" + pedido.pageId + " frame=" + pedido.frame);

        PageTableEntry[] tabela = gp.getPageTableOfProcess(pedido.pid);
        PageTableEntry pte = tabela[pedido.pageId];

        pte.present = true;
        pte.frame = pedido.frame;
        pte.onDisk = true;
        pte.dirty = false;

        gp.desbloquearProcessoById(pedido.pid);

        esc.interromperQuantum();
    }

    public synchronized void handlePageSaveComplete(PedidoIO pedido) {
        System.out.println("[INT] Página salva em disco: frame liberado = " + pedido.frame);

        gm.liberarFrame(pedido.frame);

        if (gm.haProcessoAguardandoFrame()) {
            int pid = gm.processoAguardandoFrame();
            System.out.println("[INT] Processo aguardando frame liberado: pid=" + pid);
            gp.desbloquearProcessoById(pid);
        }

        esc.interromperQuantum();
    }

    public synchronized void handle(Interrupts tipo, int pid, PedidoIO pedido) {
        switch (tipo) {

            case IO_END:
                handleIOComplete(pedido);
                break;

            case PAGE_LOAD_END:
                handlePageLoadComplete(pedido);
                break;

            case PAGE_SAVE_END:
                handlePageSaveComplete(pedido);
                break;

            case STOP:
                System.out.println("[INT] Processo finalizou: " + pid);
                gp.finalizarProcesso(pid);
                esc.interromperQuantum();
                break;

            default:
                System.out.println("[INT] Interrupção desconhecida: " + tipo);
                break;
        }
    }
}
