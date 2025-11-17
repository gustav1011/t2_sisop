public class PageTableEntry {
    public final int pageNumber;
    public int frameNumber;
    public boolean present;
    public boolean dirty;
    public boolean referenced;
    public int diskBlock;

    public PageTableEntry(int pageNumber) {
        this.pageNumber = pageNumber;
        this.frameNumber = -1;
        this.diskBlock = -1;
        this.present = false;
        this.dirty = false;
        this.referenced = false;
    }
}

