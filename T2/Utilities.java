public class Utilities {
    private final HW hw;

    public Utilities(HW _hw) {
        hw = _hw;
    }

    private void loadProgram(Word[] p) {
        Word[] m = hw.mem.pos;
        for (int i = 0; i < p.length; i++) {
            m[i].opc = p[i].opc;
            m[i].ra = p[i].ra;
            m[i].rb = p[i].rb;
            m[i].p = p[i].p;
        }
    }

    public void dump(Word w) {
        System.out.print("[ ");
        System.out.print(w.opc);
        System.out.print(", ");
        System.out.print(w.ra);
        System.out.print(", ");
        System.out.print(w.rb);
        System.out.print(", ");
        System.out.print(w.p);
        System.out.println("  ] ");
    }

    public void dump(int ini, int fim) {
        Word[] m = hw.mem.pos;
        for (int i = ini; i < fim; i++) {
            System.out.print(i);
            System.out.print(":  ");
            dump(m[i]);
        }
    }

    public void loadAndExec(Word[] p) {
        throw new UnsupportedOperationException("Execução direta não suportada no modo concorrente.");
    }
}
