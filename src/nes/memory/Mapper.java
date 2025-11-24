package nes.memory;

import ppu.PPU;

/**
 * Interface para mapeadores (NROM, MMC1, MMC3, ...).
 * Permite ao cartucho controlar como CPU e PPU acessam PRG/CHR e nametables.
 */
public interface Mapper {
    MapperType getType();

    int cpuRead(int address);
    void cpuWrite(int address, int value);

    int ppuRead(int address);
    void ppuWrite(int address, int value);

    void connect(PPU ppu);
    PPU.MirrorMode getMirrorMode();
}
