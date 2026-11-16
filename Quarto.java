import java.util.ArrayList;
import java.util.Iterator;

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

    /**
     * @return a lista de produtos consumidos neste quarto
     */
    public ArrayList<Produto> getConsumo() {
        return consumo;
    }

    /**
     * Formato de arquivo (quarto.txt): numero TAB categoria TAB diaria [TAB codigoProduto ...]
     * O consumo é gravado como códigos de produto, um por coluna (repetidos se consumiu mais de uma unidade).
     */
    public String serializar() {
        StringBuilder linha = new StringBuilder();
        linha.append(numero).append("\t").append(categoria).append("\t").append(diaria);
        Iterator<Produto> iterador = consumo.iterator();
        while (iterador.hasNext()) {
            linha.append("\t").append(iterador.next().getCodigo());
        }
        return linha.toString();
    }

    /**
     * Cria um Quarto a partir de uma linha do quarto.txt.
     * Os códigos de produto do final da linha são trocados pelos objetos Produto da pousada.
     *
     * @throws IllegalArgumentException se a linha for inválida ou citar um produto que não existe
     */
    public static Quarto deserializar(String linha, ArrayList<Produto> produtosDaPousada) {
        if (linha == null) {
            throw new IllegalArgumentException("Linha de quarto não informada");
        }
        String[] campos = linha.split("\t");
        if (campos.length < 3 || campos[1].trim().length() != 1) {
            throw new IllegalArgumentException("Linha de quarto incompleta: " + linha);
        }
        try {
            Quarto quarto = new Quarto(Integer.parseInt(campos[0].trim()),
                    campos[1].trim().charAt(0),
                    Double.parseDouble(campos[2].trim().replace(',', '.')));
            for (int i = 3; i < campos.length; i++) {
                String codigoTexto = campos[i].trim();
                if (codigoTexto.isEmpty()) {
                    continue; // colunas vazias no fim da linha são ignoradas
                }
                int codigo = Integer.parseInt(codigoTexto);
                Produto produto = buscaProdutoPorCodigo(codigo, produtosDaPousada);
                if (produto == null) {
                    throw new IllegalArgumentException("Produto " + codigo + " não existe (quarto " + campos[0].trim() + ")");
                }
                quarto.adicionaConsumo(produto);
            }
            return quarto;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Número inválido na linha de quarto: " + linha);
        }
    }

    // Procura um produto pelo código na lista informada; devolve null se não achar
    private static Produto buscaProdutoPorCodigo(int codigo, ArrayList<Produto> produtos) {
        Iterator<Produto> iterador = produtos.iterator();
        while (iterador.hasNext()) {
            Produto produto = iterador.next();
            if (produto.getCodigo() == codigo) {
                return produto;
            }
        }
        return null;
    }
}
