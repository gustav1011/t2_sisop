public class ThreadEscalonador implements Runnable {

    private final Escalonador esc;
    private final HW hw;

    public ThreadEscalonador(HW hw, Escalonador esc) {
        this.hw = hw;
        this.esc = esc;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(1); // “tick” de clock
            } catch (InterruptedException ignored) {
            }

            esc.tick();
        }
    }
}
