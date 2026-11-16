public class Produto {
    private int codigo;
    private String nome;
    private double preco;

    public Produto(int codigo, String nome, double preco) {
        setCodigo(codigo);
        setNome(nome);
        setPreco(preco);
    }

    public void setCodigo(int codigo) {
        if (codigo <= 0) {
            throw new IllegalArgumentException("Código não pode ser menor que 1");
        }
        this.codigo = codigo;
    }

    public void setNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome inválido");
        }
        this.nome = nome;
    }

    public void setPreco(double preco) {
        if (preco < 0) {
            throw new IllegalArgumentException("Preço não pode ser negativo");
        }
        this.preco = preco;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public double getPreco() {
        return preco;
    }

    @Override
    public String toString() {
        return codigo + " - " + nome + " - R$ " + String.format("%.2f", preco);
    }

    /**
     * Formato de arquivo (produto.txt): codigo TAB nome TAB preco
     */
    public String serializar() {
        return codigo + "\t" + nome + "\t" + preco;
    }

    /**
     * Cria um Produto a partir de uma linha do produto.txt.
     *
     * @throws IllegalArgumentException se a linha estiver incompleta ou com valores inválidos
     */
    public static Produto deserializar(String linha) {
        if (linha == null) {
            throw new IllegalArgumentException("Linha de produto não informada");
        }
        String[] campos = linha.split("\t");
        if (campos.length < 3) {
            throw new IllegalArgumentException("Linha de produto incompleta: " + linha);
        }
        try {
            return new Produto(Integer.parseInt(campos[0].trim()),
                    campos[1].trim(),
                    Double.parseDouble(campos[2].trim().replace(',', '.')));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Número inválido na linha de produto: " + linha);
        }
    }
}
