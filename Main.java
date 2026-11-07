public class Main {

    private static void mostraMenu() {
        System.out.println("===== POUSADA =====");
        System.out.println("1 - Consultar disponibilidade");
        System.out.println("2 - Consultar reserva");
        System.out.println("3 - Realizar reserva");
        System.out.println("4 - Cancelar reserva");
        System.out.println("5 - Realizar check-in");
        System.out.println("6 - Realizar check-out");
        System.out.println("7 - Registrar consumo");
        System.out.println("8 - Salvar");
        System.out.println("0 - Sair");
        System.out.println("Selecione uma opção");
    }

    public static void main(String[] args) {
        int opt;
        do {
            mostraMenu();
            opt = Teclado.leInt();
            switch (opt) {
                case 0:
                    System.out.println("Saindo do sistema...");
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
        } while (opt != 0);
    }
}