public class ThreadCPU implements Runnable {

    private final CPU cpu;

    public ThreadCPU(CPU cpu) {
        this.cpu = cpu;
    }

    @Override
    public void run() {
        cpu.run();
    }
}
