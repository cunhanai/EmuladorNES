package nes.memory;

import ppu.PPU;

public class NROM implements Mapper {
    private final byte[] prg;
    private final byte[] chr;
    private final boolean hasChrRam;
    private final PPU.MirrorMode mirrorMode;
    private PPU ppu;

    public NROM(byte[] prg, byte[] chr, PPU.MirrorMode mirrorMode) {
        this.prg = prg;
        this.chr = (chr == null || chr.length == 0) ? new byte[0x2000] : chr;
        this.hasChrRam = (chr == null || chr.length == 0);
        this.mirrorMode = mirrorMode;
    }

    @Override
    public MapperType getType() {
        return MapperType.NROM;
    }

    @Override
    public int cpuRead(int address) {
        if (address < 0x8000) {
            return 0;
        }
        int prgOffset = (prg.length == 0x4000)
            ? (address - 0x8000) & 0x3FFF
            : (address - 0x8000) & 0x7FFF;
        return prg[prgOffset] & 0xFF;
    }

    @Override
    public void cpuWrite(int address, int value) {
        // NROM não responde a escritas em PRG-ROM
    }

    @Override
    public int ppuRead(int address) {
        if (address < 0x2000) {
            return chr[address & 0x1FFF] & 0xFF;
        }
        return 0;
    }

    @Override
    public void ppuWrite(int address, int value) {
        if (hasChrRam && address < 0x2000) {
            chr[address & 0x1FFF] = (byte) (value & 0xFF);
        }
    }

    @Override
    public void connect(PPU ppu) {
        this.ppu = ppu;
        ppu.loadCHR(hasChrRam ? new byte[0] : chr);
        ppu.setMirrorMode(mirrorMode);
    }

    @Override
    public PPU.MirrorMode getMirrorMode() {
        return mirrorMode;
    }
}
