package leitor;

public class Chr extends Dados {

    /// true se for ROM, false se for RAM
    private boolean rom;

    public Chr(byte[] conteudo, int tamanho, int posicaoInicial) {
        super(conteudo, tamanho, posicaoInicial);
        this.rom = true;
    }

    public Chr(int posicaoInicial) {
        super(posicaoInicial);
        this.rom = false;
    }
}
