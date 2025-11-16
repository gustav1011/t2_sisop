import java.util.Scanner;

public class ThreadShell implements Runnable {

    private final Sistema sistema; // Acesso aos programas (progs)
    private final GerenteProcessos gp;
    private final GerenteMemoria gm;
    private final Escalonador esc;

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
            System.out.println("             SHELL DO SO (VM) – PUCRS");
            System.out.println("==============================================");
            // Opção 1 antiga removida pois quebra a lógica de VM
            System.out.println("1 - Criar processo (criaProcesso)");
            System.out.println("2 - Listar processos");
            System.out.println("3 - Mostrar estado da memória (Frames)");
            System.out.println("4 - Executar processos (Rodar Escalonador Manualmente)");
            System.out.println("0 - Sair");
            System.out.print("Escolha uma opção: ");

            try {
                String input = sc.nextLine();
                opcao = Integer.parseInt(input);
            } catch (Exception e) {
                opcao = -1;
            }

            switch (opcao) {
                case 1: // Criar processo
                    // Lista completa baseada na sua classe Programs
                    System.out.println("Opções: fatorial, fatorialV2, progMinimo, fibonacci10, fibonacci10v2, fibonacciREAD, PB, PC");
                    System.out.print("Nome do programa: ");
                    
                    String nome = sc.nextLine();
                    // Verifica se programa existe antes de tentar criar
                    if (sistema.progs.retrieveProgram(nome) != null) {
                        gp.criaProcesso(nome);
                    } else {
                        System.out.println("Programa '" + nome + "' não existe nos programas carregados.");
                    }
                    break;

                case 2: // Antigo case 3
                    gp.listaProcessos();
                    break;

                case 3: // Antigo case 4
                    gm.mostraFrames();
                    break;

                case 4: // Antigo case 5
                    System.out.println("Forçando execução do escalonador...");
                    esc.execAll(); 
                    break;

                case 0:
                    System.out.println("Encerrando Shell...");
                    // Opcional: Encerrar o sistema todo
                    System.exit(0);
                    break;

                default:
                    System.out.println("Opção inválida.");
            }
        }
        sc.close();
    }
}