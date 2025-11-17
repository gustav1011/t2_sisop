public class MMU {
    private final HW hw;
    private final GerenteMemoria gm;

    public MMU(HW hw, GerenteMemoria gm) {
        this.hw = hw;
        this.gm = gm;
    }

    public Word fetchWord(GerenteProcessos.PCB pcb, int logicalAddress) throws PageFaultException {
        Address addr = translate(pcb, logicalAddress);
        PageTableEntry entry = pcb.tabelaPaginas[addr.page];
        entry.referenced = true;
        return hw.mem.pos[addr.physical];
    }

    public void writeWord(GerenteProcessos.PCB pcb, int logicalAddress, Word valor) throws PageFaultException {
        Address addr = translate(pcb, logicalAddress);
        PageTableEntry entry = pcb.tabelaPaginas[addr.page];
        entry.referenced = true;
        entry.dirty = true;
        Word alvo = hw.mem.pos[addr.physical];
        alvo.opc = valor.opc;
        alvo.ra = valor.ra;
        alvo.rb = valor.rb;
        alvo.p = valor.p;
    }

    public int readData(GerenteProcessos.PCB pcb, int logicalAddress) throws PageFaultException {
        Address addr = translate(pcb, logicalAddress);
        PageTableEntry entry = pcb.tabelaPaginas[addr.page];
        entry.referenced = true;
        return hw.mem.pos[addr.physical].p;
    }

    public void writeData(GerenteProcessos.PCB pcb, int logicalAddress, int valor) throws PageFaultException {
        Address addr = translate(pcb, logicalAddress);
        PageTableEntry entry = pcb.tabelaPaginas[addr.page];
        entry.referenced = true;
        entry.dirty = true;
        Word alvo = hw.mem.pos[addr.physical];
        alvo.opc = Opcode.DATA;
        alvo.p = valor;
    }

    private Address translate(GerenteProcessos.PCB pcb, int logicalAddress) throws PageFaultException {
        int espacoLogico = pcb.tabelaPaginas.length * gm.getTamPag();
        if (logicalAddress < 0 || logicalAddress >= espacoLogico) {
            throw new PageFaultException(-1);
        }
        int tamPag = gm.getTamPag();
        int pagina = logicalAddress / tamPag;
        int desloc = logicalAddress % tamPag;
        PageTableEntry entrada = pcb.tabelaPaginas[pagina];
        if (!entrada.present || entrada.frameNumber < 0) {
            throw new PageFaultException(pagina);
        }
        int enderecoFisico = gm.enderecoFisico(entrada.frameNumber, desloc);
        return new Address(pagina, enderecoFisico);
    }

    private static class Address {
        final int page;
        final int physical;

        Address(int page, int physical) {
            this.page = page;
            this.physical = physical;
        }
    }
}
