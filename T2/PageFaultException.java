public class PageFaultException extends Exception {

    private final int enderecoLogico;
    private final int pagina;

    public PageFaultException(int enderecoLogico, int pagina) {
        super("Page Fault na página " + pagina + " (endereço lógico = " + enderecoLogico + ")");
        this.enderecoLogico = enderecoLogico;
        this.pagina = pagina;
    }

    public int getEnderecoLogico() {
        return enderecoLogico;
    }

    public int getPagina() {
        return pagina;
    }
}
