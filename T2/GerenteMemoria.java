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
        // 1. Calcula quantas páginas são necessárias baseadas no tamanho da página (ex: 16)
        int paginasNecessarias = (int) Math.ceil((double) nroPalavras / this.tamPag);
        List<Integer> framesEncontrados = new ArrayList<>();

        // 2. Procura por frames livres na memória
        for (int i = 0; i < numFrames; i++) {
            if (framesLivres[i]) {
                framesEncontrados.add(i);
                // Se já encontramos a quantidade necessária, paramos de procurar
                if (framesEncontrados.size() == paginasNecessarias) {
                    break;
                }
            }
        }

        // 3. Verifica se encontrou memória suficiente
        if (framesEncontrados.size() < paginasNecessarias) {
            System.out.println("[GerenteMemoria] Erro: Memória insuficiente para alocar " + nroPalavras + " palavras.");
            return null;
        }

        // 4. MARCA OS FRAMES COMO OCUPADOS (CRUCIAL!)
        // Se pularmos essa etapa, o próximo processo vai pegar os mesmos frames.
        for (int f : framesEncontrados) {
            framesLivres[f] = false; 
        }

        // 5. Monta o array de retorno (Tabela de Páginas simples)
        int[] tabelaPaginas = new int[paginasNecessarias];
        for (int i = 0; i < paginasNecessarias; i++) {
            tabelaPaginas[i] = framesEncontrados.get(i);
        }

        System.out.println("[GerenteMemoria] Alocadas " + paginasNecessarias + " páginas. Frames: " + framesEncontrados);
        return tabelaPaginas;
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

