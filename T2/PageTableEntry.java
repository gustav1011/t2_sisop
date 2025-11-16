public class PageTableEntry {

    public boolean present;

    public int frame;

    public boolean dirty;

    public boolean onDisk;

    public int diskSlot;

    public PageTableEntry() {
        this.present = false;
        this.frame = -1;
        this.dirty = false;
        this.onDisk = false;
        this.diskSlot = -1;
    }

    @Override
    public String toString() {
        return "[PTE present=" + present +
                ", frame=" + frame +
                ", dirty=" + dirty +
                ", onDisk=" + onDisk +
                ", diskSlot=" + diskSlot + "]";
    }
}
