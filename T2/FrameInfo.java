public class FrameInfo {
    public int frameIndex;
    public int ownerPid;
    public int pageNumber;
    public boolean occupied;

    public FrameInfo() {
        this.frameIndex = -1;
        this.occupied = false;
        this.ownerPid = -1;
        this.pageNumber = -1;
    }
}
