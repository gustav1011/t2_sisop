public class SysCallHandling {

    private final HW hw;
    private final GerenteProcessos gp;
    private final DispositivoIO dispositivo;
    private final Disco disco;
    private final GerenteMemoria gm;
    private final Escalonador esc;

    public SysCallHandling(HW hw, GerenteProcessos gp, DispositivoIO dispositivo,
            Disco disco, GerenteMemoria gm, Escalonador esc) {

        this.hw = hw;
        this.gp = gp;
        this.dispositivo = dispositivo;
        this.disco = disco;
        this.gm = gm;
        this.esc = esc;
    }

    // ----------------------------------------------------
    // FUNÇÃO CHAMADA PELA CPU QUANDO OCORRE UMA SYSCALL
    // ----------------------------------------------------
    public synchronized void handle(SysCall call, int pid, int addr) {

        switch (call) {

            case READ:
                trataRead(pid, addr);
                break;

            case WRITE:
                trataWrite(pid, addr);
                break;

            case PAGE_FAULT:
                trataPageFault(pid, addr);
                break;

            case TRAP:
                trataTrap(pid);
                break;

            default:
                System.out.println("[SysCall] Tipo desconhecido: " + call);
        }
    }

    // ---------------------
    // SYSCALL: TRAP
    // ---------------------
    private void trataTrap(int pid) {
        System.out.println("[SysCall] TRAP do processo " + pid);

        // apenas imprime algo, não bloqueia
    }

    // ---------------------
    // SYSCALL: READ
    // ---------------------
    private void trataRead(int pid, int enderecoLogico) {

        System.out.println("[SysCall] READ solicitado por pid=" + pid);

        GerenteProcessos.PCB pcb = gp.getPCB(pid);
        gp.salvarContexto(pcb);
        gp.bloquearProcesso(pcb);

        // cria pedido
        PedidoIO pedido = new PedidoIO(pid, enderecoLogico, PedidoIO.TipoIO.READ);

        dispositivo.adicionarPedido(pedido);

        esc.interromperQuantum();
    }

    // ---------------------
    // SYSCALL: WRITE
    // ---------------------
    private void trataWrite(int pid, int enderecoLogico) {

        System.out.println("[SysCall] WRITE solicitado por pid=" + pid);

        GerenteProcessos.PCB pcb = gp.getPCB(pid);
        gp.salvarContexto(pcb);
        gp.bloquearProcesso(pcb);

        PedidoIO pedido = new PedidoIO(pid, enderecoLogico, PedidoIO.TipoIO.WRITE);

        dispositivo.adicionarPedido(pedido);

        esc.interromperQuantum();
    }

    // --------------------------
    // SYSCALL: PAGE FAULT
    // --------------------------
    private void trataPageFault(int pid, int enderecoLogico) {

        System.out.println("[SysCall] PAGE FAULT pid=" + pid + " addr=" + enderecoLogico);

        GerenteProcessos.PCB pcb = gp.getPCB(pid);
        gp.salvarContexto(pcb);
        gp.bloquearProcesso(pcb);

        // QUAL página lógica
        int pageId = gm.getPagina(enderecoLogico);

        PageTableEntry[] tabela = pcb.tabelaPaginas;
        PageTableEntry pte = tabela[pageId];

        // Tenta alocar frame
        int frame = gm.alocarFrameParaPagina();

        if (frame == -1) {
            // Não há frame → vitimar página
            GerenteMemoria.Vitima v = gm.escolherVitima();
            System.out.println("[SysCall] Victim frame=" + v.frame + " (pid=" + v.pidDono + " page=" + v.pageId + ")");

            PageTableEntry pteVitima = gp.getPageTableOfProcess(v.pidDono)[v.pageId];

            // marcar como não presente
            pteVitima.present = false;

            // criar pedido de salvamento da página vitimada
            PedidoIO pedidoSalvar = new PedidoIO(
                    v.pidDono,
                    v.pageId,
                    v.frame,
                    PedidoIO.TipoIO.PAGE_SAVE);

            dispositivo.adicionarPedido(pedidoSalvar);

            esc.interromperQuantum();
            return;
        }

        // Criar pedido para carregar a página
        PedidoIO pedidoLoad = new PedidoIO(
                pid,
                pageId,
                frame,
                PedidoIO.TipoIO.PAGE_LOAD);

        dispositivo.adicionarPedido(pedidoLoad);

        pte.frame = frame;
        pte.present = false;
        pte.onDisk = true;

        esc.interromperQuantum();
    }
}
