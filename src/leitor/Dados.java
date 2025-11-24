package leitor;

public class Dados {
    protected byte[] conteudo;
    protected int tamanho;
    protected int posicaoInicial;

    public Dados(byte[] conteudo, int tamanho, int posicaoInicial) {
        this.conteudo = conteudo;
        this.tamanho = tamanho;
        this.posicaoInicial = posicaoInicial;
    }

    public Dados(int posicaoInicial) {
        this.posicaoInicial = posicaoInicial;
        this.tamanho = 0;
        this.conteudo = new byte[tamanho];
    }

    public byte[] getConteudo() {
        return conteudo;
    }

    public int getProximoByte() {
        return posicaoInicial + tamanho;
    }
}
