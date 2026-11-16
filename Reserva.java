import java.util.ArrayList;
import java.util.Iterator;

public class Reserva {
    private Data dtInicio;
    private Data dtFim;
    private String cliente;
    private Quarto quarto;
    private char status;

    public Reserva(Data dtInicio, Data dtFim, String cliente, Quarto quarto, char status) {
        setDtInicio(dtInicio);
        setDtFim(dtFim);
        setCliente(cliente);
        setQuarto(quarto);
        setStatus(status);
    }

    public Data getDtInicio() {
        return dtInicio;
    }

    public void setDtInicio(Data dtInicio) {
        if (dtInicio == null) {
            throw new IllegalArgumentException("Data inicial não pode ser vazia");
        }
        this.dtInicio = dtInicio;
    }

    public Data getDtFim() {
        return dtFim;
    }

    public void setDtFim(Data dtFim) {
        if (dtFim == null) {
            throw new IllegalArgumentException("Data final não pode ser vazia");
        }
        if (dtFim.comparaData(dtInicio) < 0) {
            throw new IllegalArgumentException("Data final anterior à inicial");
        }
        this.dtFim = dtFim;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        if (cliente == null || cliente.isBlank()) {
            throw new IllegalArgumentException("Cliente inválido");
        }
        this.cliente = cliente;
    }

    public Quarto getQuarto() {
        return quarto;
    }

    public void setQuarto(Quarto quarto) {
        if (quarto == null) {
            throw new IllegalArgumentException("Quarto não pode ser nulo");
        }
        this.quarto = quarto;
    }

    public char getStatus() {
        return status;
    }

    public void setStatus(char status) {
        if (status != 'A' && status != 'C' && status != 'I' && status != 'O') {
            throw new IllegalArgumentException("Status inválido ");
        }
        this.status = status;
    }

    @Override
    public String toString() {
        return "Cliente: " + cliente
                + " | " + dtInicio + " a " + dtFim
                + " | Quarto " + quarto;
    }

    /**
     * Formato de arquivo (reserva.txt): dtInicio TAB dtFim TAB cliente TAB numeroQuarto TAB status
     */
    public String serializar() {
        return dtInicio.serializar() + "\t" + dtFim.serializar() + "\t" + cliente + "\t"
                + quarto.getNumero() + "\t" + status;
    }

    /**
     * Calcula a quantidade de diárias da reserva.
     * Regra: número de dias entre dtInicio (inclusive) e dtFim (exclusive).
     * Ex.: check-in dia 3, check-out dia 5 → 2 diárias.
     */
    public int calculaQuantidadeDias() {
        return dtInicio.diasAte(dtFim);
    }

    /**
     * Calcula o valor total das diárias (sem consumo).
     */
    public double calculaValorDiarias() {
        return calculaQuantidadeDias() * quarto.getDiaria();
    }

    /**
     * Cria uma Reserva a partir de uma linha do reserva.txt.
     * O quarto é localizado pelo número, na lista de quartos da pousada.
     *
     * @throws IllegalArgumentException se a linha for inválida ou o quarto não existir
     */
    public static Reserva deserializar(String linha, ArrayList<Quarto> quartosDaPousada) {
        if (linha == null) {
            throw new IllegalArgumentException("Linha de reserva não informada");
        }
        String[] campos = linha.split("\t");
        if (campos.length < 5 || campos[4].trim().length() != 1) {
            throw new IllegalArgumentException("Linha de reserva incompleta: " + linha);
        }
        Data dtInicio = Data.deserializar(campos[0]);
        Data dtFim = Data.deserializar(campos[1]);
        int numeroQuarto;
        try {
            numeroQuarto = Integer.parseInt(campos[3].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Número de quarto inválido na linha de reserva: " + linha);
        }
        Quarto quarto = null;
        Iterator<Quarto> iterador = quartosDaPousada.iterator();
        while (iterador.hasNext() && quarto == null) {
            Quarto candidato = iterador.next();
            if (candidato.getNumero() == numeroQuarto) {
                quarto = candidato;
            }
        }
        if (quarto == null) {
            throw new IllegalArgumentException("Quarto " + numeroQuarto + " não existe: " + linha);
        }
        return new Reserva(dtInicio, dtFim, campos[2].trim(), quarto, campos[4].trim().charAt(0));
    }
}
