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
}
