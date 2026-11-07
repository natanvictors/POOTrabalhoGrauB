import java.lang.System;

public class Main {
    public static void main(String[] args) {
        System.out.println("Selecione uma opção");
        int opt = Teclado.leInt();
        while (true) {
            switch (opt) {
                case 0:
                    System.out.println("Saindo do sistema...");
                    System.exit(0);
                    break;
                case 1:
                    break;
                case 2:
                    break;
                case 3:
                    break;
                case 4:
                    break;
                case 5:
                    break;
                case 6:
                    break;
                case 7:
                    break;
                case 8:
                    break;
                default:
                    System.out.println("Opção inválida, tente novamente.");
                    break;
            }
        }
    }
}
