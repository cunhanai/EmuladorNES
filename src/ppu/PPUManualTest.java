package ppu;

public class PPUManualTest {
    public static void main(String[] args) {
        PPU ppu = new PPU();

        // --- Teste 1: VRAM write/read buffer (< 0x3F00) ---
        System.out.println("Teste 1: VRAM buffered read (< 0x3F00)");
        // seta vramAddr = 0x0000
        ppu.writeRegister(0x2006, 0x00);
        ppu.writeRegister(0x2006, 0x00);
        // escreve 0x12 em 0x0000
        ppu.writeRegister(0x2007, 0x12);
        // reset endereço para 0x0000 novamente
        ppu.writeRegister(0x2006, 0x00);
        ppu.writeRegister(0x2006, 0x00);
        int r1 = ppu.readRegister(0x2007);
        int r2 = ppu.readRegister(0x2007);
        System.out.printf("Leitura 1 = 0x%02X\nLeitura 2 = 0x%02X\n", r1, r2);
        // Esperado: Leitura1 = 0x00 (buffer anterior), Leitura2 = 0x12

        // --- Limpa estado antes do próximo teste ---
        ppu.reset();

        // --- Teste 2: CHR-RAM (escrita persistente em 0x0000-0x1FFF) ---
        System.out.println("\nTeste 2: CHR-RAM write/read (0x0000-0x1FFF)");
        ppu.loadCHR(null); // ativa CHR-RAM
        int addr = 0x0100;
        // seta endereço 0x0100
        ppu.writeRegister(0x2006, (addr >> 8) & 0xFF);
        ppu.writeRegister(0x2006, addr & 0xFF);
        // escreve 0x34 no padrão (CHR-RAM)
        ppu.writeRegister(0x2007, 0x34);
        // volta ao endereço 0x0100
        ppu.writeRegister(0x2006, (addr >> 8) & 0xFF);
        ppu.writeRegister(0x2006, addr & 0xFF);
        int c1 = ppu.readRegister(0x2007);
        int c2 = ppu.readRegister(0x2007);
        System.out.printf("Leitura 1 = 0x%02X\nLeitura 2 = 0x%02X\n", c1, c2);
        // Esperado: Leitura1 = 0x00 (buffer anterior) ou valor anterior do buffer, Leitura2 = 0x34 (valor escrito)

        System.out.println("\nTeste concluído.");
    }
}
