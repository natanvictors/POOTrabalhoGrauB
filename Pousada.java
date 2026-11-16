
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;

public class Pousada {

    private String nome;
    private String contato;
    private ArrayList<Quarto> quartos;
    private ArrayList<Reserva> reservas;
    private ArrayList<Produto> produtos;
    private boolean dadosNaoSalvos; // true quando há alterações ainda não gravadas em arquivo

    /**
     * Construtor sem parâmetros: cria a pousada com valores iniciais válidos.
     * Nome e contato verdadeiros são preenchidos depois, por carregaDados().
     */
    public Pousada() {
        this("Pousada", "Contato não informado");
    }

    public Pousada(String nome, String contato) {
        setNome(nome);
        setContato(contato);
        this.quartos = new ArrayList<>();
        this.reservas = new ArrayList<>();
        this.produtos = new ArrayList<>();
        this.dadosNaoSalvos = false;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome inválido");
        }
        this.nome = nome;
    }

    public String getContato() {
        return contato;
    }

    public void setContato(String contato) {
        if (contato == null || contato.isBlank()) {
            throw new IllegalArgumentException("Contato inválido");
        }
        this.contato = contato;
    }

    public ArrayList<Quarto> getQuartos() {
        return quartos;
    }

    public ArrayList<Reserva> getReservas() {
        return reservas;
    }

    public ArrayList<Produto> getProdutos() {
        return produtos;
    }

    /**
     * @return true se há alterações que ainda não foram gravadas em arquivo
     */
    public boolean temDadosNaoSalvos() {
        return dadosNaoSalvos;
    }

    // ===================== Persistência =====================
    /**
     * Formato de arquivo (pousada.txt): nome TAB contato
     */
    public String serializar() {
        return nome + "\t" + contato;
    }

    /**
     * Preenche nome e contato a partir de uma linha do pousada.txt.
     *
     * @throws IllegalArgumentException se a linha for inválida
     */
    public void deserializar(String linha) {
        if (linha == null) {
            throw new IllegalArgumentException("Linha da pousada não informada");
        }
        String[] campos = linha.split("\t");
        if (campos.length < 2) {
            throw new IllegalArgumentException("Linha da pousada incompleta: " + linha);
        }
        String nomeAnterior = nome;
        try {
            setNome(campos[0].trim());
            setContato(campos[1].trim());
        } catch (IllegalArgumentException e) {
            this.nome = nomeAnterior;
            throw e;
        }
    }

    /**
     * Lê pousada.txt, produto.txt, quarto.txt e reserva.txt, nessa ordem (o
     * quarto precisa dos produtos e a reserva precisa dos quartos). Linhas
     * inválidas são ignoradas com um aviso; arquivo ausente deixa os dados
     * vazios.
     */
    public void carregaDados() {
        quartos.clear();
        reservas.clear();
        produtos.clear();

        Iterator<String> linhasPousada = leLinhasDoArquivo("pousada.txt").iterator();
        if (linhasPousada.hasNext()) {
            String linha = linhasPousada.next();
            try {
                deserializar(linha);
            } catch (IllegalArgumentException e) {
                System.out.println("Aviso: pousada.txt ignorado (" + e.getMessage() + ")");
            }
        }

        Iterator<String> linhasProdutos = leLinhasDoArquivo("produto.txt").iterator();
        while (linhasProdutos.hasNext()) {
            String linha = linhasProdutos.next();
            try {
                produtos.add(Produto.deserializar(linha));
            } catch (IllegalArgumentException e) {
                System.out.println("Aviso: linha ignorada em produto.txt (" + e.getMessage() + ")");
            }
        }

        Iterator<String> linhasQuartos = leLinhasDoArquivo("quarto.txt").iterator();
        while (linhasQuartos.hasNext()) {
            String linha = linhasQuartos.next();
            try {
                quartos.add(Quarto.deserializar(linha, produtos));
            } catch (IllegalArgumentException e) {
                System.out.println("Aviso: linha ignorada em quarto.txt (" + e.getMessage() + ")");
            }
        }

        Iterator<String> linhasReservas = leLinhasDoArquivo("reserva.txt").iterator();
        while (linhasReservas.hasNext()) {
            String linha = linhasReservas.next();
            try {
                reservas.add(Reserva.deserializar(linha, quartos));
            } catch (IllegalArgumentException e) {
                System.out.println("Aviso: linha ignorada em reserva.txt (" + e.getMessage() + ")");
            }
        }

        dadosNaoSalvos = false;
    }

    /**
     * Grava quarto.txt e reserva.txt. As reservas canceladas (C) ou com
     * check-out (O) não são gravadas e também saem da memória, para a memória
     * ficar igual ao arquivo.
     *
     * @return true se os dois arquivos foram gravados
     */
    public boolean salvaDados() {
        ArrayList<String> linhasQuartos = new ArrayList<>();
        Iterator<Quarto> iteradorQuartos = quartos.iterator();
        while (iteradorQuartos.hasNext()) {
            linhasQuartos.add(iteradorQuartos.next().serializar());
        }

        ArrayList<String> linhasReservas = new ArrayList<>();
        Iterator<Reserva> iteradorReservas = reservas.iterator();
        while (iteradorReservas.hasNext()) {
            Reserva reserva = iteradorReservas.next();
            if (reserva.getStatus() == 'A' || reserva.getStatus() == 'I') {
                linhasReservas.add(reserva.serializar());
            }
        }

        boolean quartosGravados = gravaArquivo("quarto.txt", "numero\tcategoria\tdiaria\tconsumo", linhasQuartos);
        boolean reservasGravadas = gravaArquivo("reserva.txt", "dtInicio\tdtFim\tcliente\tquarto\tstatus", linhasReservas);
        if (!quartosGravados || !reservasGravadas) {
            return false;
        }

        // Só remove da memória depois de gravar com sucesso
        iteradorReservas = reservas.iterator();
        while (iteradorReservas.hasNext()) {
            char status = iteradorReservas.next().getStatus();
            if (status == 'C' || status == 'O') {
                iteradorReservas.remove();
            }
        }
        dadosNaoSalvos = false;
        return true;
    }

    /**
     * Lê um arquivo texto (UTF-8), ignorando a 1ª linha (cabeçalho) e as linhas
     * em branco. Se o arquivo não existir ou der erro de leitura, avisa e
     * devolve uma lista vazia.
     */
    private ArrayList<String> leLinhasDoArquivo(String nomeArquivo) {
        ArrayList<String> linhas = new ArrayList<>();
        File arquivo = new File(nomeArquivo);
        if (!arquivo.exists()) {
            System.out.println("Aviso: arquivo " + nomeArquivo + " não encontrado.");
            return linhas;
        }
        try (BufferedReader leitor = new BufferedReader(
                new InputStreamReader(new FileInputStream(arquivo), StandardCharsets.UTF_8))) {
            leitor.readLine(); // descarta o cabeçalho
            String linha = leitor.readLine();
            while (linha != null) {
                if (!linha.trim().isEmpty()) {
                    linhas.add(linha);
                }
                linha = leitor.readLine();
            }
        } catch (IOException e) {
            System.out.println("Erro ao ler " + nomeArquivo + ": " + e.getMessage());
        }
        return linhas;
    }

    /**
     * Grava um arquivo texto (UTF-8) com o cabeçalho e as linhas informadas.
     *
     * @return true se gravou com sucesso
     */
    private boolean gravaArquivo(String nomeArquivo, String cabecalho, ArrayList<String> linhas) {
        try (PrintWriter escritor = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(nomeArquivo), StandardCharsets.UTF_8))) {
            escritor.println(cabecalho);
            Iterator<String> iterador = linhas.iterator();
            while (iterador.hasNext()) {
                escritor.println(iterador.next());
            }
            return true;
        } catch (IOException e) {
            System.out.println("Erro ao gravar " + nomeArquivo + ": " + e.getMessage());
            return false;
        }
    }

    // Procura um quarto pelo número
    private Quarto buscarQuarto(int numero) {
        for (Quarto quarto : quartos) {
            if (quarto.getNumero() == numero) {
                return quarto;
            }
        }
        return null;
    }

    // Converte uma data digitada em um objeto Data
    private Data converterData(String texto) {
        try {
            return Data.deserializar(texto);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // Verifica se o quarto está disponível no período
    private boolean quartoDisponivelNoPeriodo(
            Quarto quarto, Data inicio, Data fim) {

        Reserva novaReserva
                = new Reserva(inicio, fim, "Consulta", quarto, 'A');

        for (Reserva reserva : reservas) {
            if (reserva.getQuarto().getNumero() == quarto.getNumero()
                    && reserva.estaVigente()
                    && reserva.conflitaCom(novaReserva)) {
                return false;
            }
        }

        return true;
    }

    // Verifica se o cliente possui reserva vigente
    private boolean clientePossuiReservaVigente(String cliente) {
        for (Reserva reserva : reservas) {
            if (reserva.getCliente().equalsIgnoreCase(cliente)
                    && reserva.estaVigente()) {
                return true;
            }
        }

        return false;
    }

    // Consulta a disponibilidade de um quarto em determinada data
    public void consultaDisponibilidade() {
        System.out.println("===== CONSULTAR DISPONIBILIDADE =====");

        String textoData = Teclado.leString(
                "Informe a data (dd/mm/aaaa):");

        Data data = converterData(textoData);

        if (data == null) {
            System.out.println("Data inválida!");
            return;
        }

        int numero = Teclado.leInt("Informe o número do quarto:");

        Quarto quarto = buscarQuarto(numero);

        if (quarto == null) {
            System.out.println("Quarto não encontrado!");
            return;
        }

        boolean disponivel = true;

        for (Reserva reserva : reservas) {
            if (reserva.getQuarto().getNumero() == numero
                    && reserva.estaVigente()
                    && reserva.ocupaQuartoEm(data)) {
                disponivel = false;
                break;
            }
        }

        if (disponivel) {
            System.out.println("Quarto disponível!");
            System.out.println(quarto);
        } else {
            System.out.println("Quarto indisponível nessa data!");
        }
    }

    // Consulta reservas utilizando data, nome do cliente e/ou número do quarto
    public void consultaReserva() {
        System.out.println("===== CONSULTAR RESERVA =====");

        String textoData = Teclado.leString(
                "Informe a data (dd/mm/aaaa) ou ENTER para informar o nome do cliente:");

        Data data = null;

        if (!textoData.isBlank()) {
            data = converterData(textoData);

            if (data == null) {
                System.out.println("Data inválida!");
                return;
            }
        }

        String cliente = Teclado.leString(
                "Informe o nome do cliente ou ENTER para informar o número do quarto:");

        String textoQuarto = Teclado.leString(
                "Informe o número do quarto:");

        int numeroQuarto = 0;

        if (!textoQuarto.isBlank()) {
            try {
                numeroQuarto = Integer.parseInt(textoQuarto);
            } catch (NumberFormatException e) {
                System.out.println("Número do quarto inválido!");
                return;
            }
        }

        if (data == null && cliente.isBlank() && numeroQuarto == 0) {
            System.out.println("Informe pelo menos um critério!");
            return;
        }

        boolean encontrou = false;

        for (Reserva reserva : reservas) {

            if (reserva.getStatus() != 'A' && reserva.getStatus() != 'I') {
                continue;
            }

            boolean corresponde = true;

            if (data != null && !reserva.ocupaQuartoEm(data)) {
                corresponde = false;
            }

            if (!cliente.isBlank()
                    && !reserva.getCliente().equalsIgnoreCase(cliente)) {
                corresponde = false;
            }

            if (numeroQuarto != 0
                    && reserva.getQuarto().getNumero() != numeroQuarto) {
                corresponde = false;
            }

            if (corresponde) {
                System.out.println(reserva);
                encontrou = true;
            }
        }

        if (!encontrou) {
            System.out.println("Não há reservas com esses dados!");
        }
    }

    // Realiza uma nova reserva
    public void realizaReserva() {
        System.out.println("===== REALIZAR RESERVA =====");

        String textoInicio = Teclado.leString(
                "Informe a data inicial (dd/mm/aaaa):");

        Data inicio = converterData(textoInicio);

        if (inicio == null) {
            System.out.println("Data inicial inválida!");
            return;
        }

        String textoFim = Teclado.leString(
                "Informe a data final (dd/mm/aaaa):");

        Data fim = converterData(textoFim);

        if (fim == null || fim.comparaData(inicio) <= 0) {
            System.out.println("Data final inválida!");
            return;
        }

        String cliente = Teclado.leString(
                "Informe o nome do cliente:");

        if (cliente.isBlank()) {
            System.out.println("Nome do cliente inválido!");
            return;
        }

        int numero = Teclado.leInt("Informe o número do quarto:");

        Quarto quarto = buscarQuarto(numero);

        if (quarto == null) {
            System.out.println("Quarto não encontrado!");
            return;
        }

        if (clientePossuiReservaVigente(cliente)) {
            System.out.println(
                    "O cliente já possui uma reserva ativa ou em check-in!");
            return;
        }

        if (!quartoDisponivelNoPeriodo(quarto, inicio, fim)) {
            System.out.println(
                    "O quarto não está disponível nesse período!");
            return;
        }

        Reserva novaReserva
                = new Reserva(inicio, fim, cliente, quarto, 'A');

        reservas.add(novaReserva);
        dadosNaoSalvos = true;

        System.out.println("Reserva realizada com sucesso!");
        System.out.println(novaReserva);
    }

    // Cancela uma reserva ativa do cliente
    public void cancelaReserva() {
        System.out.println("===== CANCELAR RESERVA =====");

        String cliente = Teclado.leString(
                "Informe o nome do cliente:");

        for (Reserva reserva : reservas) {

            if (reserva.getCliente().equalsIgnoreCase(cliente)
                    && reserva.getStatus() == 'A') {

                reserva.setStatus('C');
                dadosNaoSalvos = true;

                System.out.println("Reserva cancelada com sucesso!");
                return;
            }
        }

        System.out.println(
                "Não existe reserva ativa para esse cliente!");
    }
}
