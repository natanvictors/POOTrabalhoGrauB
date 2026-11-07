public class Data {
    private final int dia;
    private final int mes;
    private final int ano;

    public Data(int dia, int mes, int ano) {
        if (ano < 1) {
            throw new IllegalArgumentException("Ano inválido");
        }
        if (mes < 1 || mes > 12) {
            throw new IllegalArgumentException("Mês inválido");
        }
        if (dia < 1 || dia > diasNoMes(mes, ano)) {
            throw new IllegalArgumentException("Dia inválido");
        }
        this.dia = dia;
        this.mes = mes;
        this.ano = ano;
    }

    public int comparaData(Data outra) {
        if (ano != outra.ano) {
            return ano - outra.ano;
        }
        if (mes != outra.mes) {
            return mes - outra.mes;
        }
        return dia - outra.dia;
    }

    private int totalDias(){
        int total = 0;

        for(int a=1; a<ano; a++){
            total += anoBissexto(a) ? 366:365;
        }

        for(int m=1; m<mes; m++){
            total += diasNoMes(m, ano);
        }

        return total + dia;
    }

    public int diasAte(Data outra){
        return outra.totalDias()- this.totalDias();
    }

    public int getDia() {
        return dia;
    }

    public int getMes() {
        return mes;
    }

    public int getAno() {
        return ano;
    }

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

    private static boolean anoBissexto(int ano) {
        return ano % 4 == 0 && (ano % 100 != 0 || ano % 400 == 0);
    }

    @Override
    public String toString() {
        return String.format("%02d/%02d/%04d", dia, mes, ano);
    }
}