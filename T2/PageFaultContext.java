public class PageFaultContext {
    public final GerenteProcessos.PCB processo;
    public final PageTableEntry entrada;
    public final int frameIndex;

    public PageFaultContext(GerenteProcessos.PCB processo, PageTableEntry entrada, int frameIndex) {
        this.processo = processo;
        this.entrada = entrada;
        this.frameIndex = frameIndex;
    }
}

