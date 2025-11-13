import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GerenteMemoria {
    private final int tamMem;
    private final int tamPag;
    private final int numFrames;
    private final boolean[] framesLivres;
    private final HW hw;

    public GerenteMemoria(HW hw, int tamPag) {
        this.hw = hw;
        this.tamMem = hw.mem.pos.length;
        this.tamPag = tamPag;
        this.numFrames = tamMem / tamPag;
        this.framesLivres = new boolean[numFrames];
        Arrays.fill(framesLivres, true);
    }

    public int getTamPag() {
        return tamPag;
    }

    public int[] aloca(int nroPalavras) {
        int paginasNecessarias = (int) Math.ceil((double) nroPalavras / tamPag);
        List<Integer> frames = new ArrayList<>();

        for (int i = 0; i < numFrames && frames.size() < paginasNecessarias; i++) {
            if (framesLivres[i]) {
                frames.add(i);
            }
        }

        if (frames.size() < paginasNecessarias) {
            System.out.println("Memória insuficiente para alocar " + nroPalavras + " palavras.");
            return null;
        }

        for (int f : frames) {
            framesLivres[f] = false;
        }

        int[] tabelaPaginas = new int[paginasNecessarias];
        for (int i = 0; i < paginasNecessarias; i++) {
            tabelaPaginas[i] = frames.get(i);
        }

        System.out.println("Alocadas " + paginasNecessarias + " páginas (" + (paginasNecessarias * tamPag) + " palavras).");
        return tabelaPaginas;
    }

    public void desaloca(int[] tabelaPaginas) {
        if (tabelaPaginas == null)
            return;

        for (int frame : tabelaPaginas) {
            if (frame >= 0 && frame < numFrames) {
                framesLivres[frame] = true;
            }
        }

        System.out.println("Memória desalocada. Frames liberados: " + Arrays.toString(tabelaPaginas));
    }

    public void mostraFrames() {
        System.out.println("numFrames = " + numFrames);
        System.out.println("Estado dos frames (livres = . / ocupados = X):");
        for (int i = 0; i < numFrames; i++) {
            System.out.print(framesLivres[i] ? "." : "X");
            if ((i + 1) % 64 == 0)
                System.out.println();
        }
        System.out.println();
    }
}

