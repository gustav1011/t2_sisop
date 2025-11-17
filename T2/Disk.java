import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Disk {
    private final Map<Integer, Word[]> blocks = new HashMap<>();
    private int nextBlockId = 1;

    public synchronized int storePage(Word[] data) {
        int id = nextBlockId++;
        blocks.put(id, clonePage(data));
        return id;
    }

    public synchronized void updatePage(int blockId, Word[] data) {
        blocks.put(blockId, clonePage(data));
    }

    public synchronized Word[] readPage(int blockId) {
        Word[] stored = blocks.get(blockId);
        if (stored == null) {
            return null;
        }
        return clonePage(stored);
    }

    private Word[] clonePage(Word[] data) {
        Word[] clone = new Word[data.length];
        for (int i = 0; i < data.length; i++) {
            Word w = data[i];
            clone[i] = new Word(w.opc, w.ra, w.rb, w.p);
        }
        return clone;
    }

    public synchronized int size() {
        return blocks.size();
    }
}

