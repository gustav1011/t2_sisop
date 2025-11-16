import java.util.*;

public class GerenteMemoria {

    private final Memory mem;
    private final int tamPag;
    private final int qtdFrames;

    private final Queue<Integer> livres = new LinkedList<>();

    private final Queue<Integer> filaFIFO = new LinkedList<>();

    private final Disco disco;

    public GerenteMemoria(Memory mem, int tamPag, Disco disco) {
        this.mem = mem;
        this.tamPag = tamPag;
        this.disco = disco;

        this.qtdFrames = mem.tamMem / tamPag;

        for (int f = 0; f < qtdFrames; f++) {
            livres.add(f);
        }
    }

    public GerenteMemoria(HW hw, int tamPagina) {
        // TODO Auto-generated constructor stub
    }

    GerenteMemoria(HW hw, int tamPagina) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getTamPag() {
        return tamPag;
    }

    public int getQtdFrames() {
        return qtdFrames;
    }

    public synchronized int alocaFrame() throws PageFaultException {

        if (!livres.isEmpty()) {
            int frame = livres.poll();
            filaFIFO.add(frame);
            System.out.println("[GM] Alocado frame livre: " + frame);
            return frame;
        }

        int frameVitimado = filaFIFO.poll();

        System.out.println("[GM] Sem frames livres. Vítima = frame " + frameVitimado);

        int slot = disco.alocarSlot();

        disco.salvarPagina(slot, mem, frameVitimado, tamPag);

        livres.add(frameVitimado);

        System.out.println("[GM] Página vitimada salva no slot " + slot);

        throw new PageFaultException(frameVitimado, slot);
    }

    public synchronized void carregarPagina(Word[] programa, int pagina, PageTableEntry pte) throws PageFaultException {

        int frame = pte.frame;

        if (frame < 0) {
            frame = alocaFrame();
            pte.frame = frame;
        }

        int baseFisica = frame * tamPag;

        if (!pte.onDisk) {
            for (int i = 0; i < tamPag; i++) {
                int idxProg = pagina * tamPag + i;
                if (idxProg < programa.length) {
                    mem.pos[baseFisica + i].cloneFrom(programa[idxProg]);
                } else {
                    mem.pos[baseFisica + i].clear();
                }
            }
            System.out.println("[GM] Página " + pagina + " carregada do programa → frame " + frame);
        } else {
            disco.carregarPagina(pte.diskSlot, mem, frame, tamPag);
            System.out.println("[GM] Página " + pagina + " carregada do DISCO → frame " + frame);
        }

        pte.present = true;
    }

    public synchronized void desalocarProcesso(PageTableEntry[] tabela) {

        for (PageTableEntry pte : tabela) {
            if (pte.present && pte.frame >= 0) {
                livres.add(pte.frame);
                filaFIFO.remove(pte.frame);
            }

            if (pte.onDisk && pte.diskSlot >= 0) {
                disco.liberarSlot(pte.diskSlot);
            }
        }

        System.out.println("[GM] Processo desalocado (frames + slots liberados).");
    }

    public int traduzEndereco(int pagina, int desloc, PageTableEntry[] tabela) throws PageFaultException {

        PageTableEntry pte = tabela[pagina];

        if (!pte.present) {
            throw new PageFaultException(pagina);
        }

        return pte.frame * tamPag + desloc;
    }
}
