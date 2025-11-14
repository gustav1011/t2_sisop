
import java.util.Scanner;

public class ThreadShell implements Runnable {

    private GerenteProcessos gp;
    private GerenteMemoria gm;
    private Escalonador esc;
    private Sistema sistema;

    public ThreadShell(Sistema sistema, GerenteProcessos gp, GerenteMemoria gm, Escalonador esc) {
        this.sistema = sistema;
        this.gp = gp;
        this.gm = gm;
        this.esc = esc;
    }

    @Override
    public void run() {
        Scanner sc = new Scanner(System.in);
        int opcao = -1;

        while (opcao != 0) {
            System.out.println("\n==============================================");
            System.out.println("             SHELL DO SO – PUCRS");
            System.out.println("==============================================");
            System.out.println("1 - Executar programa direto (loadAndExec)");
            System.out.println("2 - Criar processo");
            System.out.println("3 - Listar processos");
            System.out.println("4 - Mostrar estado da memória");
            System.out.println("5 - Executar processos (forçar escalonador)");
            System.out.println("0 - Sair");
            System.out.print("Escolha uma opção: ");

            try {
                opcao = Integer.parseInt(sc.nextLine());
            } catch (Exception e) {
                opcao = -1;
            }

            switch (opcao) {
                case 1:
                    System.out.println("Nome do programa: ");
                    String nome1 = sc.nextLine();
                    Word[] prog = sistema.progs.retrieveProgram(nome1);
                    if (prog != null) {
                        sistema.so.utils.loadAndExec(prog);
                    } else {
                        System.out.println("Programa não encontrado.");
                    }
                    break;

                case 2:
                    System.out.println("Nome do programa: ");
                    String nome2 = sc.nextLine();
                    gp.criaProcesso(nome2);
                    break;

                case 3:
                    gp.listaProcessos();
                    break;

                case 4:
                    gm.mostraFrames();
                    break;

                case 5:
                    esc.execAll();
                    break;

                case 0:
                    System.out.println("Encerrando Shell...");
                    break;

                default:
                    System.out.println("Opção inválida.");
            }
        }

        sc.close();
    }
}
