import java.util.ArrayList;

public class Quarto {
    private int numero;
    private char categoria;
    private double diaria;
    private ArrayList<Produto> consumo;

    public Quarto(int numero, char categoria, double diaria) {
        setNumero(numero);
        setCategoria(categoria);
        setDiaria(diaria);
        this.consumo = new ArrayList<>();
    }

    // Adiciona um produto na lista de consumo
    public void adicionaConsumo(Produto produto) {
        if (produto == null) {
            throw new IllegalArgumentException("Produto não pode ser vazio");
        }
        consumo.add(produto);
    }

    // Imprime a lista de consumo no terminal
    public String listaConsumo() {
        String texto = "";
        for (Produto produto : consumo) {
            texto += produto.toString() + "\n";
        }
        return texto;
    }

    // Calcula o valor total da lista de consumo
    public double valorTotalConsumo() {
        double valorTotal = 0;
        for (Produto produto : consumo) {
            valorTotal += produto.getPreco();
        }
        return valorTotal;
    }

    // Limpa a lista de consumo
    public void limpaConsumo() {
        consumo.clear();
    }

    // Retorna uma versão por extenso do nome da categoria (utilizado só no toString)
    public String descricaoCategoria() {
        switch (categoria) {
            case 'S':
                return "Standard";
            case 'M':
                return "Master";
            case 'P':
                return "Premium";
            default:
                return "Desconhecida";
        }
    }


    // Getters e setters
    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        if (numero <= 0) {
            throw new IllegalArgumentException("Número não pode ser nulo ou negativo");
        }
        this.numero = numero;
    }

    public char getCategoria() {
        return categoria;
    }

    public void setCategoria(char categoria) {
        if (categoria != 'S' && categoria != 'M' && categoria != 'P') {
            throw new IllegalArgumentException("Categoria inválida");
        }
        this.categoria = categoria;
    }

    public void setDiaria(double diaria) {
        if (diaria <= 0) {
            throw new IllegalArgumentException("Diária não pode ser nula ou negativa");
        }
        this.diaria = diaria;
    }

    public double getDiaria() {
        return diaria;
    }

    @Override
    public String toString() {
        return numero + " - " + descricaoCategoria() + " - R$ " + String.format("%.2f", diaria);
    }
}
