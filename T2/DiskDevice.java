import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DiskDevice extends Thread {
    private enum TipoOperacao {
        LOAD_PAGE,
        SAVE_PAGE
    }

    private static class OperacaoDisco {
        final TipoOperacao tipo;
        final PageFaultContext contexto;
        final Word[] dados;
        final Runnable callback;

        OperacaoDisco(TipoOperacao tipo, PageFaultContext contexto, Word[] dados, Runnable callback) {
            this.tipo = tipo;
            this.contexto = contexto;
            this.dados = dados;
            this.callback = callback;
        }
    }

    private final BlockingQueue<OperacaoDisco> fila = new LinkedBlockingQueue<>();
    private final Disk disco;
    private final GerenteMemoria gm;
    private volatile boolean ativo = true;

    public DiskDevice(Disk disco, GerenteMemoria gm) {
        this.disco = disco;
        this.gm = gm;
        setName("DiskDevice");
    }

    public void solicitarCarga(PageFaultContext contexto, Runnable callback) {
        fila.offer(new OperacaoDisco(TipoOperacao.LOAD_PAGE, contexto, null, callback));
    }

    public void solicitarSalvar(PageFaultContext contexto, Word[] dados, Runnable callback) {
        fila.offer(new OperacaoDisco(TipoOperacao.SAVE_PAGE, contexto, dados, callback));
    }

    public void encerrar() {
        ativo = false;
        fila.offer(new OperacaoDisco(TipoOperacao.SAVE_PAGE, null, null, null));
    }

    @Override
    public void run() {
        while (ativo) {
            try {
                OperacaoDisco op = fila.take();
                if (!ativo || op.contexto == null) {
                    continue;
                }
                processa(op);
                if (op.callback != null) {
                    op.callback.run();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void processa(OperacaoDisco op) {
        switch (op.tipo) {
            case LOAD_PAGE:
                Word[] conteudo = disco.readPage(op.contexto.entrada.diskBlock);
                if (conteudo == null) {
                    conteudo = new Word[gm.getTamPag()];
                    for (int i = 0; i < conteudo.length; i++) {
                        conteudo[i] = new Word(Opcode.DATA, -1, -1, 0);
                    }
                }
                gm.escreveFrame(op.contexto.frameIndex, conteudo);
                break;
            case SAVE_PAGE:
                disco.updatePage(op.contexto.entrada.diskBlock, op.dados);
                break;
        }
    }
}
