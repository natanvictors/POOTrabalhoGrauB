public class Data {
    private int dia;
    private int mes;
    private int ano;

    public Data(int dia, int mes, int ano) {
        setAno(ano);
        setMes(mes);
        setDia(dia);
    }

    public int getDia() {
        return dia;
    }

    public void setDia(int dia) {
        if (dia < 1 || dia > diasNoMes(this.mes, this.ano)) {
            throw new IllegalArgumentException("Dia inválido");
        }
        this.dia = dia;
    }

    public int getMes() {
        return mes;
    }

    public void setMes(int mes) {
        if (mes < 1 || mes > 12) {
            throw new IllegalArgumentException("Mês inválido");
        }
        if (this.dia > 0 && this.dia > diasNoMes(mes, this.ano)) {
            throw new IllegalArgumentException("Dia atual inválido para o mês informado");
        }
        this.mes = mes;
    }

    public int getAno() {
        return ano;
    }

    public void setAno(int ano) {
        if (ano < 1) {
            throw new IllegalArgumentException("Ano inválido");
        }
        if (this.dia > 0 && this.mes > 0 && this.dia > diasNoMes(this.mes, ano)) {
            throw new IllegalArgumentException("Dia atual inválido para o ano informado");
        }
        this.ano = ano;
    }

    // Compara duas datas: retorna negativo se esta for anterior, 0 se igual, positivo se posterior
    public int comparaData(Data outra) {
        if (ano != outra.ano) {
            return ano - outra.ano;
        }
        if (mes != outra.mes) {
            return mes - outra.mes;
        }
        return dia - outra.dia;
    }

    // Converte a data para o total de dias corridos desde 01/01/0001
    private int totalDias() {
        int total = 0;

        for (int a = 1; a < ano; a++) {
            total += anoBissexto(a) ? 366 : 365;
        }

        for (int m = 1; m < mes; m++) {
            total += diasNoMes(m, ano);
        }

        return total + dia;
    }

    // Calcula a diferença de dias entre esta data e a data informada
    public int diasAte(Data outra) {
        return outra.totalDias() - this.totalDias();
    }

    // Retorna a quantidade de dias de um mês considerando ano bissexto
    private static int diasNoMes(int mes, int ano) {
        switch (mes) {
            case 2:
                return anoBissexto(ano) ? 29 : 28;
            case 4:
            case 6:
            case 9:
            case 11:
                return 30;
            default:
                return 31;
        }
    }

    // Verifica se o ano é bissexto
    private static boolean anoBissexto(int ano) {
        return ano % 4 == 0 && (ano % 100 != 0 || ano % 400 == 0);
    }

    @Override
    public String toString() {
        return String.format("%02d/%02d/%04d", dia, mes, ano);
    }

    /**
     * Converte a data para o formato de arquivo: dd/mm/aaaa.
     */
    public String serializar() {
        return toString();
    }

    /**
     * Cria uma Data a partir de um texto no formato dd/mm/aaaa.
     * Como os setters validam a data, 31/02 ou mês 13 também são recusados.
     *
     * @throws IllegalArgumentException se o texto não estiver no formato ou a data não existir
     */
    public static Data deserializar(String texto) {
        if (texto == null) {
            throw new IllegalArgumentException("Data não informada");
        }
        String[] partes = texto.trim().split("/");
        if (partes.length != 3) {
            throw new IllegalArgumentException("A data deve estar no formato dd/mm/aaaa: " + texto);
        }
        try {
            return new Data(Integer.parseInt(partes[0].trim()),
                    Integer.parseInt(partes[1].trim()),
                    Integer.parseInt(partes[2].trim()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("A data deve conter apenas números: " + texto);
        }
    }
}