package leitor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.MissingFormatArgumentException;

public class LeitorINES {
    private String nomeArquivoRom;

    private byte[] rom;
    private Header header;
    private Dados trainer;
    private Dados prgRom;
    private Dados chrRom;

    public LeitorINES(String nomeArquivoRom) throws IOException, MissingFormatArgumentException {
        this.nomeArquivoRom = nomeArquivoRom;
        lerROM();
        extrairHeader();
        extrairTrainer();
        extrairPrg();
        extrairChrRom();
    }

    public byte[] getPrgRom() {
        return prgRom.conteudo;
    }

    public byte[] getChrRom() {
        return chrRom != null ? chrRom.getConteudo() : null;
    }

    public Header getHeader() {
        return header;
    }

    private void lerROM() throws IOException {
        try {
            rom = Files.readAllBytes(Path.of(nomeArquivoRom));

            if (rom.length < 16)
                throw new IOException("Arquivo inexistente ou corrompido");
        }
        catch (IOException e) {
            System.err.println("Erro ao ler o arquivo: " + e.getMessage());
        }
    }

    private void extrairHeader() throws MissingFormatArgumentException {
        var header = new byte[16];
        System.arraycopy(rom, 0, header, 0, 16);
        this.header = new Header(header);
    }

    private void extrairTrainer() {
        if (header.isTemTrainer())
            this.trainer = criarDadosDoSubarray(header.getProximoByte(), 0x200);
        else
            this.trainer = new Dados(header.getProximoByte());
    }

    private void extrairPrg() {
        this.prgRom = criarDadosDoSubarray(trainer.getProximoByte(), header.getPrgRomTamanho());
    }

    private void extrairChrRom() {
        if (header.getChrRomTamanho() > 0)
            this.chrRom = criarDadosDoSubarray(prgRom.getProximoByte(), header.getChrRomTamanho());
        else
            this.chrRom = new Dados(header.getChrRomTamanho());
    }

    private Dados criarDadosDoSubarray(int posicaoInicial, int tamanho) {
        var array = new byte[tamanho];
        System.arraycopy(rom, posicaoInicial, array, 0, tamanho);
        return new Dados(array, tamanho, posicaoInicial);
    }
}

/// código morto para referencia futura
///
///             System.out.println("\n=== Mapa de Memória Simulado ===");
///             System.out.printf("PRG-ROM: 0x%04X - 0x%04X%n", 0x8000, 0x8000 + (prgRomSize * 1024) - 1);
///             if (chrRomSize > 0)
///                 System.out.printf("CHR-ROM: 0x%04X - 0x%04X%n", 0x0000, (chrRomSize * 1024) - 1);
///
///         } catch (IOException e) {
///             System.err.println("Erro ao ler o arquivo: " + e.getMessage());
///         }