package leitor;

import java.util.MissingFormatArgumentException;

public class Header extends Dados {
    private int prgRomTamanho;
    private int chrRomTamanho;
    private int mapeamento;
    private String espelhamento;
    private boolean temBattery;
    private boolean temTrainer;
    private boolean ignorarEspelhamento;

    public Header(byte[] conteudo)
    {
        super(conteudo, 16, 0);
        extrairInformacoesHeader();
    }

    public int getPrgRomTamanho() {
        return prgRomTamanho;
    }

    public int getChrRomTamanho() {
        return chrRomTamanho;
    }

    public int getMapeamento() {
        return mapeamento;
    }

    public String getEspelhamento() {
        return espelhamento;
    }

    public boolean isTemBattery() {
        return temBattery;
    }

    public boolean isTemTrainer() {
        return temTrainer;
    }

    public boolean isIgnorarEspelhamento() {
        return ignorarEspelhamento;
    }

    private void extrairInformacoesHeader() throws MissingFormatArgumentException {
        if (getConteudo()[0] != 'N'
                || getConteudo()[1] != 'E'
                || getConteudo()[2] != 'S'
                || getConteudo()[3] != 0x1A)
            throw new MissingFormatArgumentException("ROM nes.NES inválida: não é arquivo nes.NES");

        // byte 4 corresponde ao tamanho do PRG ROM em 16 unidades de kb
        prgRomTamanho = Byte.toUnsignedInt(getConteudo()[4]) * 16 * 0x400;

        // byte 5 corresponde ao tamanho do PRG ROM em 8 unidades de kb
        chrRomTamanho = Byte.toUnsignedInt(getConteudo()[5]) * 8 * 0x400;

        // byte 6 corresponde a mapeamento, espelhamento, battery, trainer
        var flag6 = Byte.toUnsignedInt(getConteudo()[6]);

        // byte 7 corresponde a mapeamento, vs/playchoice e nes.NES 2.0
        var flag7 = Byte.toUnsignedInt(getConteudo()[7]);

        mapeamento = (flag7 & 0xF0) | (flag6 >> 4);
        espelhamento = (flag6 & 0x01) != 0 ? "Vertical" : "Horizontal";

        // a baterry é persistencia salva de memória
        temBattery = (flag6 & 0x02) != 0;
        temTrainer = (flag6 & 0x04) != 0;
        ignorarEspelhamento = (flag6 & 0x08) != 0;
    }
}
