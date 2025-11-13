public class Word { // cada posicao da memoria tem uma instrucao (ou um dado)
    public Opcode opc;
    public int ra; // indice do primeiro registrador da operacao
    public int rb; // indice do segundo registrador da operacao
    public int p; // parametro ou dado

    public Word(Opcode _opc, int _ra, int _rb, int _p) {
        opc = _opc;
        ra = _ra;
        rb = _rb;
        p = _p;
    }
}

