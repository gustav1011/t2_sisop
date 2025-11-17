public class GerenteMemoria {
    private final int tamMem;
    private final int tamPag;
    private final int numFrames;
    private final FrameInfo[] frames;
    private final HW hw;
    private int proxVitima;

    public GerenteMemoria(HW hw, int tamPag) {
        this.hw = hw;
        this.tamMem = hw.mem.pos.length;
        this.tamPag = tamPag;
        this.numFrames = tamMem / tamPag;
        frames = new FrameInfo[numFrames];
        for (int i = 0; i < numFrames; i++) {
            frames[i] = new FrameInfo();
            frames[i].frameIndex = i;
        }
        this.proxVitima = 0;
    }

    public int getTamPag() {
        return tamPag;
    }

    public int getNumFrames() {
        return numFrames;
    }

    public synchronized PageTableEntry[] criaTabelaPaginas(int nroPalavras) {
        int paginasNecessarias = (int) Math.ceil((double) nroPalavras / tamPag);
        PageTableEntry[] tabela = new PageTableEntry[paginasNecessarias];
        for (int i = 0; i < paginasNecessarias; i++) {
            tabela[i] = new PageTableEntry(i);
        }
        return tabela;
    }

    public synchronized int reservarFrame(int ownerPid, int pageNumber) {
        for (int i = 0; i < frames.length; i++) {
            if (!frames[i].occupied) {
                ocuparFrame(i, ownerPid, pageNumber);
                return i;
            }
        }
        return -1;
    }

    public synchronized FrameInfo escolheVitima() {
        for (int i = 0; i < frames.length; i++) {
            int idx = (proxVitima + i) % frames.length;
            if (frames[idx].occupied) {
                proxVitima = (idx + 1) % frames.length;
                FrameInfo info = new FrameInfo();
                info.frameIndex = frames[idx].frameIndex;
                info.occupied = true;
                info.ownerPid = frames[idx].ownerPid;
                info.pageNumber = frames[idx].pageNumber;
                frames[idx].occupied = false;
                frames[idx].ownerPid = -1;
                frames[idx].pageNumber = -1;
                return info;
            }
        }
        return null;
    }

    public synchronized void ocuparFrame(int frameIndex, int ownerPid, int pageNumber) {
        frames[frameIndex].occupied = true;
        frames[frameIndex].frameIndex = frameIndex;
        frames[frameIndex].ownerPid = ownerPid;
        frames[frameIndex].pageNumber = pageNumber;
    }

    public synchronized void liberarFrame(int frameIndex) {
        frames[frameIndex].occupied = false;
        frames[frameIndex].frameIndex = frameIndex;
        frames[frameIndex].ownerPid = -1;
        frames[frameIndex].pageNumber = -1;
    }

    public synchronized FrameInfo getFrameInfo(int frameIndex) {
        FrameInfo base = frames[frameIndex];
        FrameInfo copy = new FrameInfo();
        copy.frameIndex = base.frameIndex;
        copy.occupied = base.occupied;
        copy.ownerPid = base.ownerPid;
        copy.pageNumber = base.pageNumber;
        return copy;
    }

    public int enderecoFisico(int frame, int desloc) {
        return frame * tamPag + desloc;
    }

    public synchronized Word[] dumpFrame(int frameIndex) {
        Word[] dados = new Word[tamPag];
        int inicio = frameIndex * tamPag;
        for (int i = 0; i < tamPag; i++) {
            Word orig = hw.mem.pos[inicio + i];
            dados[i] = new Word(orig.opc, orig.ra, orig.rb, orig.p);
        }
        return dados;
    }

    public synchronized void escreveFrame(int frameIndex, Word[] dados) {
        int inicio = frameIndex * tamPag;
        for (int i = 0; i < tamPag && i < dados.length; i++) {
            Word alvo = hw.mem.pos[inicio + i];
            Word origem = dados[i];
            alvo.opc = origem.opc;
            alvo.ra = origem.ra;
            alvo.rb = origem.rb;
            alvo.p = origem.p;
        }
    }

    public synchronized void mostraFrames() {
        System.out.println("numFrames = " + numFrames);
        System.out.println("Estado dos frames (livres = . / ocupados = X):");
        for (int i = 0; i < numFrames; i++) {
            System.out.print(frames[i].occupied ? "X" : ".");
            if ((i + 1) % 64 == 0)
                System.out.println();
        }
        System.out.println();
    }
}
