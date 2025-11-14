// Exceção sinalizando que o processo fez uma syscall que deve bloquear o processo.
// É capturada pelo escalonador para salvar contexto / mover processo para bloqueados.
public class SyscallBlockedException extends RuntimeException {
    public SyscallBlockedException(String message) {
        super(message);
    }
}
