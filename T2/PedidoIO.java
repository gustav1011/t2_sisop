public class PedidoIO {
    public static final int IN = 1;
    public static final int OUT = 2;

    public final int pid; // processo solicitante
    public final int rw; // IN ou OUT
    public final int endereco; // endereço de memória alvo
    public final int valor; // valor para OUT (opcional para IN)

    public PedidoIO(int pid, int rw, int endereco, int valor) {
        this.pid = pid;
        this.rw = rw;
        this.endereco = endereco;
        this.valor = valor;
    }

    @Override
    public String toString() {
        String tipo = (rw == IN) ? "IN" : (rw == OUT) ? "OUT" : "UNK";
        return "PedidoIO{pid=" + pid + ", tipo=" + tipo + ", endereco=" + endereco + ", valor=" + valor + "}";
    }
}
