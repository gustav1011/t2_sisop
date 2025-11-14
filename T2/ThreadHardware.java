public class ThreadHardware implements Runnable {

    private HW hw;
    private Escalonador esc;

    public ThreadHardware(HW hw, Escalonador esc) {
        this.hw = hw;
        this.esc = esc;
    }

    @Override
    public void run() {
        while (true) {

            try {
                Thread.sleep(100); // clock 100 ms
            } catch (InterruptedException e) {
            }

            // gera interrupção de clock
            esc.interrupcaoClock();
        }
    }
}
