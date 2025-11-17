public class CPU {
    private final int maxInt; // valores maximo e minimo para inteiros nesta cpu
    private final int minInt;
    private int pc; // program counter
    private Word ir; // instruction register
    private final int[] reg; // registradores da CPU
    private Interrupts irpt; // registro de interrupcao
    private InterruptHandling ih;
    private SysCallHandling sysCall;
    private boolean cpuStop;
    private final boolean debug;
    private Utilities u;
    private MMU mmu;
    private GerenteProcessos.PCB processoAtual;
    private int instrucoesExecutadas;
    private int ultimaPaginaFault;
    private SysCallRequest ultimaSysCall;

    public CPU(Memory _mem, boolean _debug) {
        maxInt = 32767;
        minInt = -32767;
        reg = new int[10];
        debug = _debug;
    }

    public void setAddressOfHandlers(InterruptHandling _ih, SysCallHandling _sysCall) {
        ih = _ih;
        sysCall = _sysCall;
    }

    public void setUtilities(Utilities _u) {
        u = _u;
    }

    public void setMMU(MMU _mmu) {
        mmu = _mmu;
    }

    public void setProcessoAtual(GerenteProcessos.PCB pcb) {
        processoAtual = pcb;
    }

    public GerenteProcessos.PCB getProcessoAtual() {
        return processoAtual;
    }

    public SysCallRequest getUltimaSysCall() {
        return ultimaSysCall;
    }

    private boolean testOverflow(int v) {
        if ((v < minInt) || (v > maxInt)) {
            irpt = Interrupts.intOverflow;
            return false;
        }
        return true;
    }

    public void setContext(int _pc) {
        pc = _pc;
        irpt = Interrupts.noInterrupt;
    }

    public boolean isStopped() {
        return cpuStop;
    }

    public void setStopped(boolean value) {
        cpuStop = value;
    }

    public int getPc() {
        return pc;
    }

    public int getRegister(int index) {
        return reg[index];
    }

    public void copyRegistersTo(int[] destino) {
        System.arraycopy(reg, 0, destino, 0, Math.min(reg.length, destino.length));
    }

    public void restoreRegisters(int[] origem) {
        System.arraycopy(origem, 0, reg, 0, Math.min(reg.length, origem.length));
    }

    public CPUResult run(int quantum) {
        if (processoAtual == null) {
            return new CPUResult(Interrupts.noInterrupt, true, -1, null);
        }
        instrucoesExecutadas = 0;
        ultimaPaginaFault = -1;
        ultimaSysCall = null;
        cpuStop = false;
        irpt = Interrupts.noInterrupt;
        boolean terminou = false;
        while (!cpuStop) {
            try {
                ir = mmu.fetchWord(processoAtual, pc);
            } catch (PageFaultException pf) {
                if (pf.getPageNumber() < 0) {
                    irpt = Interrupts.intEnderecoInvalido;
                } else {
                    irpt = Interrupts.intPageFault;
                    ultimaPaginaFault = pf.getPageNumber();
                }
                break;
            }

            if (debug) {
                System.out.print("                                              regs: ");
                for (int i = 0; i < 10; i++) {
                    System.out.print(" r[" + i + "]:" + reg[i]);
                }
                System.out.println();
            }
            if (debug) {
                System.out.print("                      pc: " + pc + "       exec: ");
                u.dump(ir);
            }

            switch (ir.opc) {
                case LDI:
                    reg[ir.ra] = ir.p;
                    pc++;
                    break;
                case LDD:
                    try {
                        reg[ir.ra] = mmu.readData(processoAtual, ir.p);
                        pc++;
                    } catch (PageFaultException e) {
                        tratarPageFault(e);
                    }
                    break;
                case LDX:
                    try {
                        reg[ir.ra] = mmu.readData(processoAtual, reg[ir.rb]);
                        pc++;
                    } catch (PageFaultException e) {
                        tratarPageFault(e);
                    }
                    break;
                case STD:
                    try {
                        mmu.writeData(processoAtual, ir.p, reg[ir.ra]);
                        pc++;
                    } catch (PageFaultException e) {
                        tratarPageFault(e);
                    }
                    break;
                case STX:
                    try {
                        mmu.writeData(processoAtual, reg[ir.ra], reg[ir.rb]);
                        pc++;
                    } catch (PageFaultException e) {
                        tratarPageFault(e);
                    }
                    break;
                case MOVE:
                    reg[ir.ra] = reg[ir.rb];
                    pc++;
                    break;
                case ADD:
                    reg[ir.ra] = reg[ir.ra] + reg[ir.rb];
                    testOverflow(reg[ir.ra]);
                    pc++;
                    break;
                case ADDI:
                    reg[ir.ra] = reg[ir.ra] + ir.p;
                    testOverflow(reg[ir.ra]);
                    pc++;
                    break;
                case SUB:
                    reg[ir.ra] = reg[ir.ra] - reg[ir.rb];
                    testOverflow(reg[ir.ra]);
                    pc++;
                    break;
                case SUBI:
                    reg[ir.ra] = reg[ir.ra] - ir.p;
                    testOverflow(reg[ir.ra]);
                    pc++;
                    break;
                case MULT:
                    reg[ir.ra] = reg[ir.ra] * reg[ir.rb];
                    testOverflow(reg[ir.ra]);
                    pc++;
                    break;
                case JMP:
                    pc = ir.p;
                    break;
                case JMPIM:
                    try {
                        pc = mmu.readData(processoAtual, ir.p);
                    } catch (PageFaultException e) {
                        tratarPageFault(e);
                    }
                    break;
                case JMPIG:
                    if (reg[ir.rb] > 0) {
                        pc = reg[ir.ra];
                    } else {
                        pc++;
                    }
                    break;
                case JMPIGK:
                    if (reg[ir.rb] > 0) {
                        pc = ir.p;
                    } else {
                        pc++;
                    }
                    break;
                case JMPILK:
                    if (reg[ir.rb] < 0) {
                        pc = ir.p;
                    } else {
                        pc++;
                    }
                    break;
                case JMPIEK:
                    if (reg[ir.rb] == 0) {
                        pc = ir.p;
                    } else {
                        pc++;
                    }
                    break;
                case JMPIL:
                    if (reg[ir.rb] < 0) {
                        pc = reg[ir.ra];
                    } else {
                        pc++;
                    }
                    break;
                case JMPIE:
                    if (reg[ir.rb] == 0) {
                        pc = reg[ir.ra];
                    } else {
                        pc++;
                    }
                    break;
                case JMPIGM:
                    try {
                        if (reg[ir.rb] > 0) {
                            pc = mmu.readData(processoAtual, ir.p);
                        } else {
                            pc++;
                        }
                    } catch (PageFaultException e) {
                        tratarPageFault(e);
                    }
                    break;
                case JMPILM:
                    try {
                        if (reg[ir.rb] < 0) {
                            pc = mmu.readData(processoAtual, ir.p);
                        } else {
                            pc++;
                        }
                    } catch (PageFaultException e) {
                        tratarPageFault(e);
                    }
                    break;
                case JMPIEM:
                    try {
                        if (reg[ir.rb] == 0) {
                            pc = mmu.readData(processoAtual, ir.p);
                        } else {
                            pc++;
                        }
                    } catch (PageFaultException e) {
                        tratarPageFault(e);
                    }
                    break;
                case JMPIGT:
                    if (reg[ir.ra] > reg[ir.rb]) {
                        pc = ir.p;
                    } else {
                        pc++;
                    }
                    break;
                case DATA:
                    irpt = Interrupts.intInstrucaoInvalida;
                    break;
                case SYSCALL:
                    ultimaSysCall = sysCall.buildRequest(processoAtual);
                    irpt = Interrupts.intSyscall;
                    pc++;
                    break;
                case STOP:
                    sysCall.stop();
                    terminou = true;
                    cpuStop = true;
                    break;
                default:
                    irpt = Interrupts.intInstrucaoInvalida;
                    break;
            }

            if (irpt != Interrupts.noInterrupt) {
                cpuStop = true;
                break;
            }

            instrucoesExecutadas++;
            if (quantum > 0 && instrucoesExecutadas >= quantum) {
                irpt = Interrupts.intTimer;
                cpuStop = true;
                break;
            }
        }

        if (processoAtual != null) {
            processoAtual.pc = pc;
        }

        if (irpt != Interrupts.noInterrupt && ih != null) {
            ih.handle(irpt);
        }
        return new CPUResult(irpt, terminou, ultimaPaginaFault, ultimaSysCall);
    }

    private void tratarPageFault(PageFaultException e) {
        if (e.getPageNumber() < 0) {
            irpt = Interrupts.intEnderecoInvalido;
        } else {
            irpt = Interrupts.intPageFault;
            ultimaPaginaFault = e.getPageNumber();
        }
        cpuStop = true;
    }
}
