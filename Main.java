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
    }

    // Lê um número inteiro válido do terminal; repete se digitar letra ou vazio
    private static int leInteiro(String mensagem) {
        while (true) {
            System.out.println(mensagem);
            String texto = Teclado.leString();
            if (texto == null) {
                return 0;
            }
            try {
                return Integer.parseInt(texto.trim());
            } catch (NumberFormatException e) {
                System.out.println("Valor inválido. Digite apenas números inteiros.");
            }
        }
    }
    
    // Lê uma data no formato dd/mm/aaaa; se permitirVazio for true, Enter retorna null
    private static Data leData(String mensagem, boolean permitirVazio) {
        while (true) {
            System.out.println(mensagem);
            String texto = Teclado.leString();
            if (permitirVazio && (texto == null || texto.isBlank())) {
                return null;
            }
            try {
                return Data.deserializar(texto);
            } catch (Exception e) {
                System.out.println("Data inválida. Use o formato dd/mm/aaaa.");
            }
        }
    }

    public static void main(String[] args) {
        Pousada pousada = new Pousada();
        pousada.carregaDados();

        int opt;
        do {
            mostraMenu();
            opt = leInteiro("Selecione uma opção:");
            switch (opt) {
                case 0:
                    // Salva automaticamente se houver alterações pendentes
                    if (pousada.temDadosNaoSalvos()) {
                        System.out.println("Salvando alterações pendentes...");
                        pousada.salvaDados();
                    }
                    System.out.println("Saindo do sistema...");
                    break;

                case 1: // Consultar disponibilidade
                    Data dataDisp = leData("Informe a data (dd/mm/aaaa):", false);
                    int quartoDisp = leInteiro("Informe o número do quarto:");
                    System.out.println(pousada.consultaDisponibilidade(dataDisp, quartoDisp));
                    break;

                case 2: // Consultar reserva
                    Data dataCons = leData("Informe a data (dd/mm/aaaa) ou Enter para ignorar:", true);
                    System.out.println("Informe o nome do cliente ou Enter para ignorar:");
                    String clienteCons = Teclado.leString();
                    int quartoCons = leInteiro("Informe o número do quarto ou 0 para ignorar:");
                    System.out.println(pousada.consultaReserva(dataCons, clienteCons, quartoCons));
                    break;

                case 3: // Realizar reserva
                    Data dtIni = leData("Informe a data inicial (dd/mm/aaaa):", false);
                    Data dtFim = leData("Informe a data final (dd/mm/aaaa):", false);
                    System.out.println("Informe o nome do cliente:");
                    String clienteRes = Teclado.leString();
                    int quartoRes = leInteiro("Informe o número do quarto:");
                    System.out.println(pousada.realizaReserva(dtIni, dtFim, clienteRes, quartoRes));
                    break;

                case 4: // Cancelar reserva
                    System.out.println("Informe o nome do cliente:");
                    String clienteCanc = Teclado.leString();
                    System.out.println(pousada.cancelaReserva(clienteCanc));
                    break;

                case 5: // Realizar check-in
                    System.out.println("Informe o nome do cliente para check-in:");
                    String clienteCheckIn = Teclado.leString();
                    System.out.println(pousada.realizaCheckIn(clienteCheckIn));
                    break;

                case 6: // Realizar check-out
                    System.out.println("Informe o nome do cliente para check-out:");
                    String clienteCheckOut = Teclado.leString();
                    System.out.println(pousada.realizaCheckOut(clienteCheckOut));
                    break;

                case 7: // Registrar consumo
                    System.out.println("Informe o nome do cliente:");
                    String clienteConsumo = Teclado.leString();
                    if (!pousada.clienteTemCheckIn(clienteConsumo)) {
                        System.out.println("Erro: não há reserva em check-in para o cliente " + clienteConsumo + ".");
                    } else {
                        System.out.println(pousada.listaProdutosDisponiveis());
                        int codigoProduto = leInteiro("Informe o código do produto:");
                        System.out.println(pousada.registraConsumo(clienteConsumo, codigoProduto));
                    }
                    break;

                case 8: // Salvar
                    if (pousada.salvaDados()) {
                        System.out.println("Dados salvos com sucesso!");
                    } else {
                        System.out.println("Erro ao salvar os dados.");
                    }
                    break;

                default:
                    System.out.println("Opção inválida, tente novamente.");
                    break;
            }
        } while (opt != 0);
    }
}