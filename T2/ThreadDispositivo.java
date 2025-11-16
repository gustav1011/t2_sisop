public class ThreadDispositivo implements Runnable {

    private final DispositivoIO dispositivo;

    public ThreadDispositivo(DispositivoIO dispositivo) {
        this.dispositivo = dispositivo;
    }

    @Override
    public void run() {
        dispositivo.run();
    }
}
