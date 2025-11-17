import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class Escalonador extends Thread {
    private final GerenteProcessos gp;
    private final HW hw;
    private final int quantum;
    private final GerenteMemoria gm;
    private final MMU mmu;
    private final Disk disco;

    private final CpuWorker cpuWorker;
    private final ConsoleDevice console;
    private final DiskDevice diskDevice;
    private final Map<Integer, PageFaultContext> faltas = new ConcurrentHashMap<>();
    private volatile boolean executando = true;

    public Escalonador(GerenteProcessos gp, HW hw, int quantum, GerenteMemoria gm, MMU mmu, Disk disco) {
        this.gp = gp;
        this.hw = hw;
        this.quantum = quantum;
        this.gm = gm;
        this.mmu = mmu;
        this.disco = disco;
        this.cpuWorker = new CpuWorker(gp, hw.cpu);
        this.console = new ConsoleDevice(mmu, this);
        this.diskDevice = new DiskDevice(disco, gm);
        setName("Escalonador");
    }

    public void iniciar() {
        cpuWorker.start();
        console.start();
        diskDevice.start();
        start();
    }

    public void encerrar() {
        executando = false;
        cpuWorker.encerrar();
        console.encerrar();
        diskDevice.encerrar();
        this.interrupt();
    }

    @Override
    public void run() {
        while (executando) {
            GerenteProcessos.PCB pcb = gp.proximoProcesso();
            if (pcb == null) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                continue;
            }

            gp.setRodando(pcb);
            try {
                CompletableFuture<CPUResult> futuro = cpuWorker.executar(pcb, quantum);
                CPUResult resultado = futuro.get();
                tratarResultado(pcb, resultado);
            } catch (Exception e) {
                System.out.println("Erro durante execucao do processo " + pcb.id + ": " + e.getMessage());
            } finally {
                gp.limparRodando();
            }
        }
    }

    private void tratarResultado(GerenteProcessos.PCB pcb, CPUResult resultado) {
        if (resultado.halted) {
            System.out.println("Processo " + pcb.id + " finalizado.");
            gp.desalocaProcesso(pcb.id);
            return;
        }

        switch (resultado.interrupt) {
            case intTimer:
                gp.refileiraProcesso(pcb);
                break;
            case intSyscall:
                if (resultado.sysCall != null) {
                    gp.bloquearProcesso(pcb);
                    console.solicitar(resultado.sysCall);
                } else {
                    gp.refileiraProcesso(pcb);
                }
                break;
            case intPageFault:
                tratarPageFault(pcb, resultado.pageFault);
                break;
            case intEnderecoInvalido:
            case intInstrucaoInvalida:
            case intOverflow:
                System.out.println("Processo " + pcb.id + " abortado por " + resultado.interrupt);
                gp.desalocaProcesso(pcb.id);
                break;
            default:
                gp.refileiraProcesso(pcb);
                break;
        }
    }

    private void tratarPageFault(GerenteProcessos.PCB pcb, int pagina) {
        if (pagina < 0 || pagina >= pcb.tabelaPaginas.length) {
            System.out.println("Page fault invalido no processo " + pcb.id);
            gp.desalocaProcesso(pcb.id);
            return;
        }
        PageTableEntry entrada = pcb.tabelaPaginas[pagina];
        gp.bloquearProcesso(pcb);
        int frame = gm.reservarFrame(pcb.id, pagina);

        if (frame >= 0) {
            PageFaultContext contexto = new PageFaultContext(pcb, entrada, frame);
            faltas.put(pcb.id, contexto);
            diskDevice.solicitarCarga(contexto, () -> onPageLoaded(contexto));
        } else {
            FrameInfo vitima = gm.escolheVitima();
            if (vitima == null) {
                System.out.println("Nao foi possivel encontrar vitima para page fault.");
                return;
            }
            GerenteProcessos.PCB pcbVitima = gp.getProcesso(vitima.ownerPid);
            if (pcbVitima == null) {
                gm.ocuparFrame(vitima.frameIndex, pcb.id, pagina);
                PageFaultContext novoContexto = new PageFaultContext(pcb, entrada, vitima.frameIndex);
                faltas.put(pcb.id, novoContexto);
                diskDevice.solicitarCarga(novoContexto, () -> onPageLoaded(novoContexto));
                return;
            }
            PageTableEntry entradaVitima = pcbVitima.tabelaPaginas[vitima.pageNumber];
            PageFaultContext ctxVitima = new PageFaultContext(pcbVitima, entradaVitima, vitima.frameIndex);
            Word[] dados = gm.dumpFrame(vitima.frameIndex);
            entradaVitima.present = false;
            entradaVitima.frameNumber = -1;
            gm.ocuparFrame(vitima.frameIndex, pcb.id, pagina);
            PageFaultContext novoContexto = new PageFaultContext(pcb, entrada, vitima.frameIndex);
            faltas.put(pcb.id, novoContexto);
            diskDevice.solicitarSalvar(ctxVitima, dados,
                    () -> diskDevice.solicitarCarga(novoContexto, () -> onPageLoaded(novoContexto)));
        }
    }

    public void notificaIOConclusao(SysCallRequest req) {
        gp.desbloquearProcesso(req.processo);
    }

    private void onPageLoaded(PageFaultContext contexto) {
        contexto.entrada.present = true;
        contexto.entrada.frameNumber = contexto.frameIndex;
        contexto.entrada.dirty = false;
        contexto.entrada.referenced = false;
        faltas.remove(contexto.processo.id);
        gp.desbloquearProcesso(contexto.processo);
    }
}
