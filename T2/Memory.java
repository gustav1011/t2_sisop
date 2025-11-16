public class Memory {

    public final Word[] pos;

    public Memory(int tamanho) {
        pos = new Word[tamanho];
        for (int i = 0; i < tamanho; i++) {
            pos[i] = new Word(null, i, i, i);
        }
        System.out.println("[Memory] Memória criada com " + tamanho + " palavras.");
    }

    public int tamanho() {
        return pos.length;
    }

    public Word read(int endereco) {
        if (endereco < 0 || endereco >= pos.length) {
            throw new RuntimeException("Acesso a endereço inválido da memória: " + endereco);
        }
        return pos[endereco];
    }

    public void write(int endereco, Word w) {
        if (endereco < 0 || endereco >= pos.length) {
            throw new RuntimeException("Escrita em endereço inválido da memória: " + endereco);
        }
        pos[endereco].opc = w.opc;
        pos[endereco].ra = w.ra;
        pos[endereco].rb = w.rb;
        pos[endereco].p = w.p;
    }
}
