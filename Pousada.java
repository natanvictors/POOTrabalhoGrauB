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

    // ===================== Estadia =====================

    /**
     * Busca a reserva com check-in ativo (status 'I') para o quarto informado.
     * Retorna null se não encontrar.
     */
    public Reserva buscaReservaEmCheckInDoQuarto(int numeroQuarto) {
        Iterator<Reserva> iterador = reservas.iterator();
        while (iterador.hasNext()) {
            Reserva reserva = iterador.next();
            if (reserva.getStatus() == 'I'
                    && reserva.getQuarto().getNumero() == numeroQuarto) {
                return reserva;
            }
        }
        return null;
    }

    /**
     * Realiza o check-in de uma reserva (status 'A' → 'I').
     * Regras:
     *   - O quarto deve existir.
     *   - Deve haver exatamente UMA reserva com status 'A' para esse quarto.
     *   - Não pode já haver outra reserva com status 'I' no mesmo quarto.
     *
     * @param numeroQuarto número do quarto
     * @return mensagem de sucesso ou de erro
     */
    public String realizaCheckIn(int numeroQuarto) {
        // Verifica se o quarto existe
        Quarto quartoEncontrado = null;
        Iterator<Quarto> itQ = quartos.iterator();
        while (itQ.hasNext()) {
            Quarto q = itQ.next();
            if (q.getNumero() == numeroQuarto) {
                quartoEncontrado = q;
            }
        }
        if (quartoEncontrado == null) {
            return "Erro: quarto " + numeroQuarto + " não existe.";
        }

        // Verifica se já há check-in ativo no quarto
        if (buscaReservaEmCheckInDoQuarto(numeroQuarto) != null) {
            return "Erro: quarto " + numeroQuarto + " já possui check-in ativo.";
        }

        // Busca reserva com status 'A' para o quarto
        Reserva reservaParaCheckIn = null;
        int countAtivas = 0;
        Iterator<Reserva> itR = reservas.iterator();
        while (itR.hasNext()) {
            Reserva r = itR.next();
            if (r.getQuarto().getNumero() == numeroQuarto && r.getStatus() == 'A') {
                reservaParaCheckIn = r;
                countAtivas++;
            }
        }

        if (countAtivas == 0) {
            return "Erro: não há reserva ativa (status A) para o quarto " + numeroQuarto + ".";
        }
        if (countAtivas > 1) {
            return "Erro: há mais de uma reserva ativa para o quarto " + numeroQuarto + ". Informe o cliente.";
        }

        reservaParaCheckIn.setStatus('I');
        dadosNaoSalvos = true;
        return "Check-in realizado com sucesso!\n"
                + "Quarto  : " + numeroQuarto
                + " | Cliente : " + reservaParaCheckIn.getCliente()
                + " | Período : " + reservaParaCheckIn.getDtInicio()
                + " a " + reservaParaCheckIn.getDtFim();
    }

    /**
     * Realiza o check-out de uma reserva (status 'I' → 'O').
     * Monta o comprovante com diárias e consumo antes de limpar o consumo.
     *
     * @param numeroQuarto número do quarto
     * @return comprovante ou mensagem de erro
     */
    public String realizaCheckOut(int numeroQuarto) {
        Reserva reserva = buscaReservaEmCheckInDoQuarto(numeroQuarto);
        if (reserva == null) {
            return "Erro: não há check-in ativo para o quarto " + numeroQuarto + ".";
        }

        Quarto quarto = reserva.getQuarto();
        double valorDiarias = reserva.calculaValorDiarias();
        double valorConsumo = quarto.valorTotalConsumo();
        double totalGeral = valorDiarias + valorConsumo;

        // Monta o comprovante ANTES de limpar o consumo
        StringBuilder comprovante = new StringBuilder();
        comprovante.append("========== COMPROVANTE DE CHECK-OUT ==========\n");
        comprovante.append("Cliente  : ").append(reserva.getCliente()).append("\n");
        comprovante.append("Quarto   : ").append(quarto.toString()).append("\n");
        comprovante.append("Período  : ").append(reserva.getDtInicio())
                   .append(" a ").append(reserva.getDtFim()).append("\n");
        comprovante.append("Diárias  : ").append(reserva.calculaQuantidadeDias())
                   .append(" x R$ ").append(String.format("%.2f", quarto.getDiaria()))
                   .append(" = R$ ").append(String.format("%.2f", valorDiarias)).append("\n");

        if (valorConsumo > 0) {
            comprovante.append("--- Consumo ---\n");
            comprovante.append(quarto.listaConsumo());
            comprovante.append("Subtotal consumo: R$ ").append(String.format("%.2f", valorConsumo)).append("\n");
        } else {
            comprovante.append("Consumo  : nenhum\n");
        }

        comprovante.append("----------------------------------------------\n");
        comprovante.append("TOTAL    : R$ ").append(String.format("%.2f", totalGeral)).append("\n");
        comprovante.append("==============================================");

        // Limpa o consumo e finaliza a reserva
        quarto.limpaConsumo();
        reserva.setStatus('O');
        dadosNaoSalvos = true;

        return comprovante.toString();
    }

    /**
     * Registra o consumo de um produto no quarto que está com check-in ativo.
     *
     * @param numeroQuarto  número do quarto
     * @param codigoProduto código do produto
     * @return mensagem de sucesso ou de erro
     */
    public String registraConsumo(int numeroQuarto, int codigoProduto) {
        Reserva reserva = buscaReservaEmCheckInDoQuarto(numeroQuarto);
        if (reserva == null) {
            return "Erro: não há check-in ativo para o quarto " + numeroQuarto + ".";
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
                + " → Quarto " + numeroQuarto;
    }
}
