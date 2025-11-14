public class SysCallHandling {
    private final HW hw;
    private final GerenteProcessos gp;
    private final DispositivoIO dispositivo;

    public SysCallHandling(HW _hw, GerenteProcessos _gp, DispositivoIO _dispositivo) {
        hw = _hw;
        gp = _gp;
        dispositivo = _dispositivo;
    }

    public void stop() {
        System.out.println("                                               SYSCALL STOP");
    }

    /**
     * Agora enfileira pedido de IO e sinaliza bloqueio via SyscallBlockedException.
     * O escalonador captura a exceção e faz salvar contexto / mover processo para
     * BLOQUEADO.
     */
    public void handle() {
        int rw = hw.cpu.getRegister(8); // 1 = IN, 2 = OUT (convenção)
        int endereco = hw.cpu.getRegister(9);

        System.out.println("[SysCallHandling] SYSCALL pars: rw=" + rw + " / endereco=" + endereco);

        GerenteProcessos.PCB rodando = gp.getRodando();
        if (rodando == null) {
            System.out.println("[SysCallHandling] Nenhum processo rodando ao chamar syscall.");
            return;
        }

        if (rw == PedidoIO.IN || rw == PedidoIO.OUT) {
            int valor = 0;
            if (rw == PedidoIO.OUT) {
                // valor pode já estar na memória (STD) — opcional pegar
                valor = hw.mem.pos[endereco].p;
            }
            PedidoIO pedido = new PedidoIO(rodando.id, rw, endereco, valor);
            dispositivo.submit(pedido);

            // sinaliza para o escalonador que o processo deve ser bloqueado agora
            throw new SyscallBlockedException("Processo " + rodando.id + " bloqueado por SYSCALL (rw=" + rw + ")");
        } else {
            System.out.println("[SysCallHandling] PARAMETRO INVALIDO: rw=" + rw);
        }
    }
}
