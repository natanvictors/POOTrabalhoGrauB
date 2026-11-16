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
}
