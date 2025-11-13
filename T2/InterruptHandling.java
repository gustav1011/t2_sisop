public class InterruptHandling {
    private final HW hw;

    public InterruptHandling(HW _hw) {
        hw = _hw;
    }

    public void handle(Interrupts irpt) {
        System.out.println(
                "                                               Interrupcao " + irpt + "   pc: " + hw.cpu.getPc());
    }
}

