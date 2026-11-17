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

    @Override
    public String toString() {
        return nome + " (" + contato + ")";
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
            this.nome = nomeAnterior; // desfaz a alteração parcial para não deixar a pousada inconsistente
            throw e;
        }
    }

    /**
     * Lê pousada.txt, produto.txt, quarto.txt e reserva.txt, nessa ordem
     * (o quarto precisa dos produtos e a reserva precisa dos quartos).
     * Linhas inválidas são ignoradas com um aviso; arquivo ausente deixa os dados vazios.
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
     * Grava quarto.txt e reserva.txt. As reservas canceladas (C) ou com check-out (O)
     * não são gravadas e também saem da memória, para a memória ficar igual ao arquivo.
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
     * Lê um arquivo texto (UTF-8), ignorando a 1ª linha (cabeçalho) e as linhas em branco.
     * Se o arquivo não existir ou der erro de leitura, avisa e devolve uma lista vazia.
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

    // ===================== Métodos auxiliares de busca =====================

    // Procura um quarto pelo número; devolve null se não existir
    private Quarto buscaQuarto(int numeroQuarto) {
        Iterator<Quarto> iterador = quartos.iterator();
        while (iterador.hasNext()) {
            Quarto q = iterador.next();
            if (q.getNumero() == numeroQuarto) {
                return q;
            }
        }
        return null;
    }

    // Verifica se o quarto está ocupado na data informada (status 'A' ou 'I')
    private boolean quartoOcupadoNaData(int numeroQuarto, Data data) {
        Iterator<Reserva> iterador = reservas.iterator();
        while (iterador.hasNext()) {
            Reserva r = iterador.next();
            if (r.getQuarto().getNumero() == numeroQuarto
                    && (r.getStatus() == 'A' || r.getStatus() == 'I')) {
                // Ocupado se data >= dtInicio e data < dtFim
                if (data.comparaData(r.getDtInicio()) >= 0 && data.comparaData(r.getDtFim()) < 0) {
                    return true;
                }
            }
        }
        return false;
    }

    // Verifica se dois períodos [ini1, fim1) e [ini2, fim2) se sobrepõem
    private boolean periodosConflitam(Data ini1, Data fim1, Data ini2, Data fim2) {
        return ini1.comparaData(fim2) < 0 && fim1.comparaData(ini2) > 0;
    }

    // Busca reserva de um cliente por status específico ('A' ou 'I')
    private Reserva buscaReservaPorClienteEStatus(String cliente, char status) {
        if (cliente == null || cliente.isBlank()) {
            return null;
        }
        Iterator<Reserva> iterador = reservas.iterator();
        while (iterador.hasNext()) {
            Reserva r = iterador.next();
            if (r.getStatus() == status && r.getCliente().equalsIgnoreCase(cliente.trim())) {
                return r;
            }
        }
        return null;
    }

    // ===================== Consultas e Reservas =====================

    // Opção 1: verifica se um quarto está livre em uma data específica
    public String consultaDisponibilidade(Data data, int numeroQuarto) {
        if (data == null) {
            return "Erro: data inválida.";
        }
        Quarto quarto = buscaQuarto(numeroQuarto);
        if (quarto == null) {
            return "Erro: quarto " + numeroQuarto + " não existe.";
        }
        if (quartoOcupadoNaData(numeroQuarto, data)) {
            return "O quarto " + numeroQuarto + " não está disponível na data " + data + ".";
        }
        return "Quarto disponível na data " + data + ":\n" + quarto.toString();
    }

    // Opção 2: consulta reservas ativas ('A') filtrando por data e/ou cliente e/ou quarto
    public String consultaReserva(Data data, String cliente, int numeroQuarto) {
        boolean filtraData = (data != null);
        boolean filtraCliente = (cliente != null && !cliente.isBlank());
        boolean filtraQuarto = (numeroQuarto > 0);

        StringBuilder resultado = new StringBuilder();
        int encontradas = 0;

        Iterator<Reserva> iterador = reservas.iterator();
        while (iterador.hasNext()) {
            Reserva r = iterador.next();
            if (r.getStatus() != 'A') {
                continue;
            }
            if (filtraData && (data.comparaData(r.getDtInicio()) < 0 || data.comparaData(r.getDtFim()) > 0)) {
                continue;
            }
            if (filtraCliente && !r.getCliente().equalsIgnoreCase(cliente.trim())) {
                continue;
            }
            if (filtraQuarto && r.getQuarto().getNumero() != numeroQuarto) {
                continue;
            }

            resultado.append(r.toString()).append("\n");
            encontradas++;
        }

        if (encontradas == 0) {
            return "Não há reservas ativas com os parâmetros informados.";
        }
        return "Reservas encontradas:\n" + resultado.toString().trim();
    }

    // Opção 3: cria uma nova reserva se o quarto estiver livre no período e o cliente não tiver reserva 'A' ou 'I'
    public String realizaReserva(Data dtIni, Data dtFim, String cliente, int numeroQuarto) {
        if (dtIni == null || dtFim == null) {
            return "Erro: datas inválidas.";
        }
        if (dtFim.comparaData(dtIni) <= 0) {
            return "Erro: a data final deve ser posterior à data inicial.";
        }
        if (cliente == null || cliente.isBlank()) {
            return "Erro: nome do cliente inválido.";
        }

        Quarto quarto = buscaQuarto(numeroQuarto);
        if (quarto == null) {
            return "Erro: quarto " + numeroQuarto + " não existe.";
        }

        // Verifica se o cliente já tem reserva ativa ('A') ou em check-in ('I')
        if (buscaReservaPorClienteEStatus(cliente, 'A') != null
                || buscaReservaPorClienteEStatus(cliente, 'I') != null) {
            return "Erro: o cliente " + cliente.trim() + " já possui uma reserva ativa ou em check-in.";
        }

        // Verifica se há conflito de datas no quarto solicitado
        Iterator<Reserva> iterador = reservas.iterator();
        while (iterador.hasNext()) {
            Reserva r = iterador.next();
            if (r.getQuarto().getNumero() == numeroQuarto
                    && (r.getStatus() == 'A' || r.getStatus() == 'I')
                    && periodosConflitam(dtIni, dtFim, r.getDtInicio(), r.getDtFim())) {
                return "Erro: o quarto " + numeroQuarto + " já está reservado ou ocupado nesse período.";
            }
        }

        Reserva novaReserva = new Reserva(dtIni, dtFim, cliente.trim(), quarto, 'A');
        reservas.add(novaReserva);
        dadosNaoSalvos = true;

        return "Reserva realizada com sucesso!\n" + novaReserva.toString();
    }

    // Opção 4: cancela a reserva ativa ('A') do cliente
    public String cancelaReserva(String cliente) {
        Reserva reserva = buscaReservaPorClienteEStatus(cliente, 'A');
        if (reserva == null) {
            return "Erro: não existe reserva ativa para o cliente " + cliente + ".";
        }
        reserva.setStatus('C');
        dadosNaoSalvos = true;
        return "Reserva de " + reserva.getCliente() + " (Quarto " + reserva.getQuarto().getNumero() + ") cancelada com sucesso.";
    }

    // ===================== Estadia e Consumo =====================

    // Opção 5: realiza check-in da reserva ativa ('A') do cliente
    public String realizaCheckIn(String cliente) {
        Reserva reserva = buscaReservaPorClienteEStatus(cliente, 'A');
        if (reserva == null) {
            return "Erro: não existe reserva ativa para o cliente " + cliente + ".";
        }

        // Garante que o quarto não esteja ocupado por outro hóspede no momento
        Iterator<Reserva> iterador = reservas.iterator();
        while (iterador.hasNext()) {
            Reserva r = iterador.next();
            if (r.getStatus() == 'I' && r.getQuarto().getNumero() == reserva.getQuarto().getNumero()) {
                return "Erro: o quarto " + reserva.getQuarto().getNumero() + " ainda possui um check-in ativo.";
            }
        }

        reserva.setStatus('I');
        dadosNaoSalvos = true;

        int dias = reserva.calculaQuantidadeDias();
        double valorDiarias = reserva.calculaValorDiarias();

        return "Check-in realizado com sucesso!\n"
                + "Cliente  : " + reserva.getCliente() + "\n"
                + "Período  : " + reserva.getDtInicio() + " a " + reserva.getDtFim() + " (" + dias + " diárias)\n"
                + "Total diárias: R$ " + String.format("%.2f", valorDiarias) + "\n"
                + "Quarto   : " + reserva.getQuarto().toString();
    }

    // Opção 6: realiza check-out do cliente com check-in ativo ('I')
    public String realizaCheckOut(String cliente) {
        Reserva reserva = buscaReservaPorClienteEStatus(cliente, 'I');
        if (reserva == null) {
            return "Erro: não existe reserva em check-in para o cliente " + cliente + ".";
        }

        Quarto quarto = reserva.getQuarto();
        double valorDiarias = reserva.calculaValorDiarias();
        double valorConsumo = quarto.valorTotalConsumo();
        double totalGeral = valorDiarias + valorConsumo;

        // Monta o comprovante antes de limpar o consumo do quarto
        StringBuilder comprovante = new StringBuilder();
        comprovante.append("========== COMPROVANTE DE CHECK-OUT ==========\n");
        comprovante.append("Cliente  : ").append(reserva.getCliente()).append("\n");
        comprovante.append("Quarto   : ").append(quarto.toString()).append("\n");
        comprovante.append("Período  : ").append(reserva.getDtInicio())
                   .append(" a ").append(reserva.getDtFim())
                   .append(" (").append(reserva.calculaQuantidadeDias()).append(" diárias)\n");
        comprovante.append("Diárias  : R$ ").append(String.format("%.2f", valorDiarias)).append("\n");

        if (valorConsumo > 0) {
            comprovante.append("--- Consumo da Copa ---\n");
            comprovante.append(quarto.listaConsumo());
            comprovante.append("Total consumo: R$ ").append(String.format("%.2f", valorConsumo)).append("\n");
        } else {
            comprovante.append("Consumo  : nenhum\n");
        }

        comprovante.append("----------------------------------------------\n");
        comprovante.append("VALOR FINAL A PAGAR: R$ ").append(String.format("%.2f", totalGeral)).append("\n");
        comprovante.append("==============================================");

        quarto.limpaConsumo();
        reserva.setStatus('O');
        dadosNaoSalvos = true;

        return comprovante.toString();
    }

    // Verifica se o cliente possui check-in ativo ('I')
    public boolean clienteTemCheckIn(String cliente) {
        return buscaReservaPorClienteEStatus(cliente, 'I') != null;
    }

    // Lista os produtos cadastrados na copa
    public String listaProdutosDisponiveis() {
        if (produtos.isEmpty()) {
            return "Nenhum produto cadastrado na copa.";
        }
        StringBuilder lista = new StringBuilder("--- Produtos Disponíveis ---\n");
        Iterator<Produto> iterador = produtos.iterator();
        while (iterador.hasNext()) {
            lista.append(iterador.next().toString()).append("\n");
        }
        return lista.toString().trim();
    }

    // Opção 7: registra o consumo de 1 unidade do produto para o cliente em check-in
    public String registraConsumo(String cliente, int codigoProduto) {
        Reserva reserva = buscaReservaPorClienteEStatus(cliente, 'I');
        if (reserva == null) {
            return "Erro: não existe reserva em check-in para o cliente " + cliente + ".";
        }

        Produto produtoEncontrado = null;
        Iterator<Produto> itP = produtos.iterator();
        while (itP.hasNext()) {
            Produto p = itP.next();
            if (p.getCodigo() == codigoProduto) {
                produtoEncontrado = p;
            }
        }
        if (produtoEncontrado == null) {
            return "Erro: produto com código " + codigoProduto + " não existe.";
        }

        reserva.getQuarto().adicionaConsumo(produtoEncontrado);
        dadosNaoSalvos = true;
        return "Consumo registrado: " + produtoEncontrado.getNome()
                + " (R$ " + String.format("%.2f", produtoEncontrado.getPreco()) + ")"
                + " -> Quarto " + reserva.getQuarto().getNumero();
    }
}