import java.util.HashMap;
import java.util.Map;

public class Disco {

    private final Map<Integer, Word[]> slots = new HashMap<>();

    private int nextSlot = 0;

    public synchronized int salvarPagina(Word[] pagina) {
        int slot = nextSlot++;
        slots.put(slot, copiaPagina(pagina));
        return slot;
    }

    public synchronized Word[] lerPagina(int slot) {
        Word[] pag = slots.get(slot);
        if (pag == null) {
            System.out.println("[DISCO] ERRO: slot " + slot + " não encontrado!");
            return null;
        }
        return copiaPagina(pag);
    }

    public synchronized void removerPagina(int slot) {
        slots.remove(slot);
    }

    public synchronized boolean existe(int slot) {
        return slots.containsKey(slot);
    }

    private Word[] copiaPagina(Word[] original) {
        Word[] copia = new Word[original.length];
        for (int i = 0; i < original.length; i++) {
            copia[i] = new Word(null, i, i, i);
            copia[i].opc = original[i].opc;
            copia[i].ra = original[i].ra;
            copia[i].rb = original[i].rb;
            copia[i].p = original[i].p;
        }
        return copia;
    }
}
