public class PageFaultException extends Exception {
    private final int pageNumber;

    public PageFaultException(int pageNumber) {
        super("Page fault on page " + pageNumber);
        this.pageNumber = pageNumber;
    }

    public int getPageNumber() {
        return pageNumber;
    }
}

