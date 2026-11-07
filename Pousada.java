import java.util.ArrayList;

public class Pousada {
    private String nome;
    private String contato;
    private ArrayList<Quarto> quartos;
    private ArrayList<Reserva> reservas;
    private ArrayList<Produto> produtos;

    public Pousada(String nome, String contato) {
        setNome(nome);
        setContato(contato);
        this.quartos = new ArrayList<>();
        this.reservas = new ArrayList<>();
        this.produtos = new ArrayList<>();
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
}