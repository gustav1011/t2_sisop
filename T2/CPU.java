public class CPU {
    private final int maxInt; // valores maximo e minimo para inteiros nesta cpu
    private final int minInt;
    private int pc; // program counter
    private Word ir; // instruction register
    private final int[] reg; // registradores da CPU
    private Interrupts irpt; // registro de interrupcao
    private final Word[] m; // memoria fisica
    private InterruptHandling ih;
    private SysCallHandling sysCall;
    private boolean cpuStop;
    private final boolean debug;
    private Utilities u;

    public CPU(Memory _mem, boolean _debug) {
        maxInt = 32767;
        minInt = -32767;
        m = _mem.pos;
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

    private boolean legal(int e) {
        if (e >= 0 && e < m.length) {
            return true;
        } else {
            irpt = Interrupts.intEnderecoInvalido;
            return false;
        }
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

    public void run() {
        cpuStop = false;
        while (!cpuStop) {
            if (legal(pc)) {
                ir = m[pc];
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
                        if (legal(ir.p)) {
                            reg[ir.ra] = m[ir.p].p;
                            pc++;
                        }
                        break;
                    case LDX:
                        if (legal(reg[ir.rb])) {
                            reg[ir.ra] = m[reg[ir.rb]].p;
                            pc++;
                        }
                        break;
                    case STD:
                        if (legal(ir.p)) {
                            m[ir.p].opc = Opcode.DATA;
                            m[ir.p].p = reg[ir.ra];
                            pc++;
                            if (debug) {
                                System.out.print("                                  ");
                                u.dump(ir.p, ir.p + 1);
                            }
                        }
                        break;
                    case STX:
                        if (legal(reg[ir.ra])) {
                            m[reg[ir.ra]].opc = Opcode.DATA;
                            m[reg[ir.ra]].p = reg[ir.rb];
                            pc++;
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
                        pc = m[ir.p].p;
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
                        if (legal(ir.p)) {
                            if (reg[ir.rb] > 0) {
                                pc = m[ir.p].p;
                            } else {
                                pc++;
                            }
                        }
                        break;
                    case JMPILM:
                        if (reg[ir.rb] < 0) {
                            pc = m[ir.p].p;
                        } else {
                            pc++;
                        }
                        break;
                    case JMPIEM:
                        if (reg[ir.rb] == 0) {
                            pc = m[ir.p].p;
                        } else {
                            pc++;
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
                        sysCall.handle();
                        pc++;
                        break;
                    case STOP:
                        sysCall.stop();
                        cpuStop = true;
                        break;
                    default:
                        irpt = Interrupts.intInstrucaoInvalida;
                        break;
                }
            }

            if (irpt != Interrupts.noInterrupt) {
                ih.handle(irpt);
                cpuStop = true;
            }
        }
    }
}

