public class PedidoIO {

    public enum Tipo {
        LEITURA,
        ESCRITA,
        PAGE_IN,
        PAGE_OUT
    }

    public final int pid;
    public final Tipo tipo;
    public final int page;
    public final int frame;
    public final int diskSlot;

    public PedidoIO(int pid, Tipo tipo, int page, int frame, int diskSlot) {
        this.pid = pid;
        this.tipo = tipo;
        this.page = page;
        this.frame = frame;
        this.diskSlot = diskSlot;
    }

    @Override
    public String toString() {
        return "[IO pid=" + pid +
                ", tipo=" + tipo +
                ", page=" + page +
                ", frame=" + frame +
                ", diskSlot=" + diskSlot + "]";
    }
}
