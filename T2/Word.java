public class Word {
    public Opcode opc;
    public int ra;
    public int rb;
    public int p;

    public Word(Opcode _opc, int _ra, int _rb, int _p) {
        this.opc = _opc;
        this.ra = _ra;
        this.rb = _rb;
        this.p = _p;
    }

    public Word() {
        this(Opcode.___, -1, -1, -1);
    }

    public Word copy() {
        return new Word(this.opc, this.ra, this.rb, this.p);
    }

    @Override
    public String toString() {
        return "[" + opc + ", " + ra + ", " + rb + ", " + p + "]";
    }
}
