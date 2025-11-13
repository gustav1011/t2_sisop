import java.util.Scanner;

public class Sistema {

    public final HW hw;
    public final SO so;
    public final Programs progs;

    public Sistema(int tamMem) {
        hw = new HW(tamMem); // memoria do HW tem tamMem palavras
        so = new SO(hw);
        hw.cpu.setUtilities(so.utils); // permite cpu fazer dump de memoria ao avancar
        progs = new Programs();
    }

    public void run() {
        so.utils.loadAndExec(progs.retrieveProgram("fatorialV2"));
        so.utils.loadAndExec(progs.retrieveProgram("fatorial"));
        // fibonacci10,
        // fibonacci10v2,
        // progMinimo,
        // fatorialWRITE, // saida
        // fibonacciREAD, // entrada
        // PB
        // PC, // bubble sort
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        Sistema s = new Sistema(1024);
        GerenteMemoria gm = new GerenteMemoria(s.hw, 16);
        GerenteProcessos gp = new GerenteProcessos(gm, s.hw, s.progs);
        Escalonador esc = new Escalonador(gp, s.hw, 5);

        int opcao = -1;

        while (opcao != 0) {
            System.out.println("\n==============================================");
            System.out.println("   MENU DO SISTEMA OPERACIONAL - PUCRS");
            System.out.println("==============================================");
            System.out.println("1 - Executar programa direto (loadAndExec)");
            System.out.println("2 - Criar processo (Gerente de Processos)");
            System.out.println("3 - Listar processos");
            System.out.println("4 - Mostrar estado da memória");
            System.out.println("5 - Executar todos os processos (Escalonador)");
            System.out.println("0 - Sair");
            System.out.print("Escolha uma opção: ");

            try {
                opcao = Integer.parseInt(sc.nextLine());
            } catch (Exception e) {
                opcao = -1;
            }

            switch (opcao) {
                case 1:
                    System.out.println("\n--- Programas disponíveis ---");
                    System.out.println("fatorial | fatorialV2 | fibonacci10 | progMinimo | PC | PB");
                    System.out.print("Digite o nome do programa: ");
                    String nomeProg = sc.nextLine();
                    Word[] prog = s.progs.retrieveProgram(nomeProg);
                    if (prog != null) {
                        s.so.utils.loadAndExec(prog);
                    } else {
                        System.out.println("Programa não encontrado.");
                    }
                    break;

                case 2:
                    System.out.println("\n--- Programas disponíveis ---");
                    System.out.println("fatorial | fatorialV2 | fibonacci10 | progMinimo | PC | PB");
                    System.out.print("Digite o nome do programa: ");
                    String nomeProc = sc.nextLine();
                    gp.criaProcesso(nomeProc);
                    break;

                case 3:
                    gp.listaProcessos();
                    break;

                case 4:
                    gm.mostraFrames();
                    break;

                case 5:
                    if (!gp.haProcessosProntos()) {
                        System.out.println("Fila de prontos vazia.");
                    } else {
                        esc.execAll();
                    }
                    break;

                case 0:
                    System.out.println("Encerrando...");
                    break;

                default:
                    System.out.println("Opção inválida.");
                    break;
            }
        }

        sc.close();
    }
}

