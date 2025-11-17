import java.util.Scanner;

public class Shell implements Runnable {
    private final Sistema sistema;
    private final Scanner scanner = new Scanner(System.in);
    private volatile boolean executando = true;

    public Shell(Sistema sistema) {
        this.sistema = sistema;
    }

    @Override
    public void run() {
        System.out.println("Shell iniciado. Digite 'help' para lista de comandos.");
        while (executando) {
            System.out.print("sisop> ");
            String linha = scanner.nextLine();
            if (linha == null) {
                continue;
            }
            tratarComando(linha.trim());
        }
        scanner.close();
    }

    private void tratarComando(String linha) {
        if (linha.isEmpty()) {
            return;
        }
        String[] partes = linha.split("\\s+");
        String cmd = partes[0].toLowerCase();
        switch (cmd) {
            case "help":
                System.out.println("Comandos: ");
                System.out.println("  run <programa>  - cria novo processo");
                System.out.println("  list            - lista processos");
                System.out.println("  mem             - mostra frames");
                System.out.println("  kill <pid>      - desaloca processo");
                System.out.println("  exit            - encerra o sistema");
                break;
            case "run":
                if (partes.length < 2) {
                    System.out.println("Uso: run <programa>");
                    break;
                }
                sistema.criaProcesso(partes[1]);
                break;
            case "list":
                sistema.listaProcessos();
                break;
            case "mem":
                sistema.mostraMemoria();
                break;
            case "kill":
                if (partes.length < 2) {
                    System.out.println("Uso: kill <pid>");
                    break;
                }
                try {
                    int pid = Integer.parseInt(partes[1]);
                    sistema.desalocaProcesso(pid);
                } catch (NumberFormatException e) {
                    System.out.println("PID inválido.");
                }
                break;
            case "exit":
                executando = false;
                sistema.encerrar();
                break;
            default:
                System.out.println("Comando desconhecido. Use 'help'.");
        }
    }
}

