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
        Pousada pousada = new Pousada();
        pousada.carregaDados();

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
                case 5: // Check-in
                    System.out.println("Número do quarto para check-in:");
                    int quartoCheckIn = Teclado.leInt();
                    System.out.println(pousada.realizaCheckIn(quartoCheckIn));
                    break;
                case 6: // Check-out
                    System.out.println("Número do quarto para check-out:");
                    int quartoCheckOut = Teclado.leInt();
                    System.out.println(pousada.realizaCheckOut(quartoCheckOut));
                    break;
                case 7: // Registrar consumo
                    System.out.println("Número do quarto:");
                    int quartoConsumo = Teclado.leInt();
                    System.out.println("Código do produto:");
                    int codigoProduto = Teclado.leInt();
                    System.out.println(pousada.registraConsumo(quartoConsumo, codigoProduto));
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
