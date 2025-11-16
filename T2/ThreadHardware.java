public class ThreadHardware implements Runnable {

    private final HW hw;
    private final Escalonador esc;
    private final int clockMs;

    /**
     * @param hw      Referência ao hardware
     * @param esc     Escalonador que vai receber interrupções de clock
     * @param clockMs tempo do “tic” do clock em ms
     */
    public ThreadHardware(HW hw, Escalonador esc, int clockMs) {
        this.hw = hw;
        this.esc = esc;
        this.clockMs = clockMs;
    }

    @Override
    public void run() {
        System.out.println("[Clock] ThreadHardware iniciada. Tick=" + clockMs + "ms");

        while (true) {
            try {
                Thread.sleep(clockMs);
            } catch (InterruptedException e) {
                System.out.println("[Clock] Interrompido.");
                return;
            }

            esc.interrupcaoClock();
        }
    }
}
