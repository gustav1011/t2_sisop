public class HW {
    public final Memory mem;
    public final CPU cpu;

    public HW(int tamMem) {
        mem = new Memory(tamMem);
        cpu = new CPU(mem, true); // true liga debug
    }
}

