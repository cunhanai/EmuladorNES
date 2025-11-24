// java
package ppu;

import nes.memory.Mapper;

/**
 * PPU (Picture Processing Unit) do nes.NES
 * Responsável por renderizar gráficos
 */
public class PPU {
    // Resolução do nes.NES: 256x240 pixels
    public static final int SCREEN_WIDTH = 256;
    public static final int SCREEN_HEIGHT = 240;

    // Framebuffer: array de pixels (formato RGB)
    private int[] framebuffer;

    private boolean debugPattern = false;

    // Registradores PPU
    private int ppuCtrl;    // $2000
    private int ppuMask;    // $2001
    private int ppuStatus;  // $2002
    private int oamAddr;    // $2003
    private int ppuScroll;  // $2005
    private int ppuAddr;    // $2006 (valor exposto)
    private int ppuData;    // $2007 (valor exposto)

    // Memória interna
    private byte[] vram;    // 2KB Video RAM (nametables)
    private byte[] oam;     // 256 bytes Object Attribute Memory (sprites)
    private byte[] palette; // 32 bytes paleta de cores
    // CHR ROM (pattern tables) ou CHR RAM
    private byte[] chrRom;
    private byte[] chrRam;
    private boolean hasChrRam = true;

    // Read buffer para PPUDATA
    private int readBuffer = 0;

    // Contadores
    private int scanline;   // Linha atual (0-261)
    private int cycle;      // Ciclo atual na linha (0-340)
    private long frame;     // Frame atual

    // Flags
    private boolean nmiOccurred;
    private boolean renderingEnabled;
    private boolean sprite0Hit; // novo: flag de colisão do sprite 0

    // Registradores internos de endereço/scroll (modelo mais fiel)
    private int vramAddr;     // v: endereço VRAM atual (15 bits, usamos 14)
    private int tempVramAddr; // t: registrador temporário (15 bits)
    private int fineX;        // x: scroll fino X (3 bits)
    private boolean writeToggle; // w: flip-flop de escrita para PPUSCROLL/PPUADDR

    // Background rendering shift registers e buffers
    private int bgShiftPatternLow = 0;
    private int bgShiftPatternHigh = 0;
    private int bgShiftAttribLow = 0;
    private int bgShiftAttribHigh = 0;

    // Next tile data
    private int bgNextTileId = 0;
    private int bgNextTileAttrib = 0;
    private int bgNextTileLsb = 0;
    private int bgNextTileMsb = 0;

    private MirrorMode mirrorMode = MirrorMode.HORIZONTAL;

    private Mapper mapper;

    public enum MirrorMode {
        HORIZONTAL,
        VERTICAL,
        FOUR_SCREEN,
        SINGLE_SCREEN_LOW,
        SINGLE_SCREEN_HIGH
    }

    /**
     * Construtor da PPU
     */
    public PPU() {
        framebuffer = new int[SCREEN_WIDTH * SCREEN_HEIGHT];
        vram = new byte[2048];
        oam = new byte[256];
        palette = new byte[32];
        chrRom = new byte[0];
        chrRam = new byte[0x2000];
        hasChrRam = true;
        reset();
    }

    /**
     * Reset da PPU
     */
    public void reset() {
        ppuCtrl = 0;
        ppuMask = 0;
        ppuStatus = 0;
        oamAddr = 0;
        ppuScroll = 0;
        ppuAddr = 0;
        ppuData = 0;

        scanline = 0;
        cycle = 0;
        frame = 0;

        nmiOccurred = false;
        renderingEnabled = false;
        sprite0Hit = false;

        // Limpa o framebuffer
        for (int i = 0; i < framebuffer.length; i++) {
            framebuffer[i] = 0xFF000000; // Preto
        }

        vramAddr = 0;
        tempVramAddr = 0;
        fineX = 0;
        writeToggle = false;

        readBuffer = 0;
    }

    /**
     * Executa um ciclo da PPU
     * A PPU roda 3 vezes mais rápido que a CPU
     */
    public void step() {
        cycle++;
        if (cycle > 340) {
            cycle = 0;
            scanline++;
            if (scanline > 261) {
                scanline = 0;
                frame++;
            }
        }

        // VBlank inicia na linha 241
        if (scanline == 241 && cycle == 1) {
            ppuStatus |= 0x80; // Set VBlank flag (bit 7)
            if ((ppuCtrl & 0x80) != 0) { // NMI habilitado
                nmiOccurred = true;
            }
        }

        // VBlank termina na linha 261 (pré-render)
        if (scanline == 261 && cycle == 1) {
            ppuStatus &= ~0x80; // Clear VBlank flag
            nmiOccurred = false;
            sprite0Hit = false; // limpa sprite 0 hit a cada frame
            ppuStatus &= ~0x40; // garante bit 6 limpo no início do frame
        }

        // Rendering logic (scanlines 0-239 e pre-render scanline 261)
        if (scanline < 240 || scanline == 261) {
            // Atualiza shift registers e busca tiles
            if ((cycle >= 2 && cycle < 258) || (cycle >= 321 && cycle < 338)) {
                updateShifters();

                switch ((cycle - 1) % 8) {
                    case 0:
                        loadBackgroundShifters();
                        bgNextTileId = ppuRead(0x2000 | (vramAddr & 0x0FFF)) & 0xFF;
                        break;
                    case 2:
                        // Fetch attribute byte
                        int attrAddr = 0x23C0 | (vramAddr & 0x0C00) | ((vramAddr >> 4) & 0x38) | ((vramAddr >> 2) & 0x07);
                        bgNextTileAttrib = ppuRead(attrAddr) & 0xFF;
                        if ((vramAddr & 0x0040) != 0) bgNextTileAttrib >>= 4;
                        if ((vramAddr & 0x0002) != 0) bgNextTileAttrib >>= 2;
                        bgNextTileAttrib &= 0x03;
                        break;
                    case 4:
                        int patternBase = (ppuCtrl & 0x10) != 0 ? 0x1000 : 0x0000;
                        int fineY = (vramAddr >> 12) & 0x07;
                        bgNextTileLsb = ppuRead(patternBase + (bgNextTileId << 4) + fineY) & 0xFF;
                        break;
                    case 6:
                        int patternBase2 = (ppuCtrl & 0x10) != 0 ? 0x1000 : 0x0000;
                        int fineY2 = (vramAddr >> 12) & 0x07;
                        bgNextTileMsb = ppuRead(patternBase2 + (bgNextTileId << 4) + fineY2 + 8) & 0xFF;
                        break;
                    case 7:
                        incrementScrollX();
                        break;
                }
            }

            if (cycle == 256) {
                incrementScrollY();
            }

            if (cycle == 257) {
                loadBackgroundShifters();
                transferAddressX();
            }

            if (cycle == 338 || cycle == 340) {
                bgNextTileId = ppuRead(0x2000 | (vramAddr & 0x0FFF)) & 0xFF;
            }

            if (scanline == 261 && cycle >= 280 && cycle < 305) {
                transferAddressY();
            }
        }

        // Renderiza linhas visíveis (0-239)
        if (scanline < 240 && cycle >= 1 && cycle <= 256) {
            renderPixel();
        }
    }

    /**
     * Renderiza um pixel simples (background + sprites básicos)
     */
    private void renderPixel() {
        int x = cycle - 1;
        int y = scanline;
        if (x < 0 || x >= SCREEN_WIDTH || y < 0 || y >= SCREEN_HEIGHT) return;

        boolean showBackground = (ppuMask & 0x08) != 0;
        boolean showSprites = (ppuMask & 0x10) != 0;
        boolean backgroundEnabledAtX = showBackground && (x >= 8 || (ppuMask & 0x02) != 0);
        boolean spritesEnabledAtX = showSprites && (x >= 8 || (ppuMask & 0x04) != 0);

        int bgPaletteLocalIndex = 0;
        int bgColor = getBackgroundColor();

        if (backgroundEnabledAtX && showBackground) {
            // Use shift registers to get background pixel
            int bitMux = 0x8000 >> fineX;
            int p0 = (bgShiftPatternLow & bitMux) != 0 ? 1 : 0;
            int p1 = (bgShiftPatternHigh & bitMux) != 0 ? 1 : 0;
            bgPaletteLocalIndex = (p1 << 1) | p0;

            if (bgPaletteLocalIndex != 0) {
                int pal0 = (bgShiftAttribLow & bitMux) != 0 ? 1 : 0;
                int pal1 = (bgShiftAttribHigh & bitMux) != 0 ? 1 : 0;
                int paletteHighBits = (pal1 << 1) | pal0;
                int paletteIndexInBytes = (paletteHighBits << 2) | bgPaletteLocalIndex;
                int paletteEntryIndex = paletteIndexInBytes & 0x0F;
                int paletteAddr = 0x3F00 + paletteEntryIndex;
                int paletteIndex = ppuRead(paletteAddr) & 0x3F;
                bgColor = getNESColor(paletteIndex);
            }
        }

        boolean bgHasVisiblePixel = bgPaletteLocalIndex != 0 && backgroundEnabledAtX;
        int finalColor = bgColor;

        if (spritesEnabledAtX) {
            // Cada sprite ocupa 4 bytes na OAM: Y, tileIndex, atributos, X
            for (int i = 0; i < 64; i++) {
                int base = i * 4;
                int spriteY = (oam[base] & 0xFF);
                int tileIndex = oam[base + 1] & 0xFF;
                int attributes = oam[base + 2] & 0xFF;
                int spriteX = oam[base + 3] & 0xFF;

                int spriteHeight = 8; // modo 8x8 apenas por enquanto

                if (x < spriteX || x >= spriteX + 8 || y < spriteY || y >= spriteY + spriteHeight) {
                    continue;
                }

                int pixelX = x - spriteX;
                int pixelY = y - spriteY;

                boolean flipHorizontal = (attributes & 0x40) != 0;
                boolean flipVertical = (attributes & 0x80) != 0;

                if (flipHorizontal) {
                    pixelX = 7 - pixelX;
                }
                if (flipVertical) {
                    pixelY = 7 - pixelY;
                }

                int patternBase = (ppuCtrl & 0x08) != 0 ? 0x1000 : 0x0000;
                int patternAddr = patternBase + tileIndex * 16 + pixelY;

                int lowPlane = ppuRead(patternAddr);
                int highPlane = ppuRead(patternAddr + 8);

                int bit0 = (lowPlane >> (7 - pixelX)) & 0x01;
                int bit1 = (highPlane >> (7 - pixelX)) & 0x01;
                int paletteIndexLocal = (bit1 << 1) | bit0;

                if (paletteIndexLocal == 0) {
                    continue;
                }

                int paletteHighBits = attributes & 0x03;
                int paletteIndexInBytes = (paletteHighBits << 2) | paletteIndexLocal;

                int paletteEntryIndex = paletteIndexInBytes & 0x0F;
                int paletteAddr = 0x3F10 + paletteEntryIndex;
                int paletteIndex = ppuRead(paletteAddr) & 0x3F;

                int spriteColor = getNESColor(paletteIndex);
                boolean spriteBehindBackground = (attributes & 0x20) != 0;

                if (i == 0 && bgHasVisiblePixel && x < 255) {
                    sprite0Hit = true;
                    ppuStatus |= 0x40;
                }

                if (!spriteBehindBackground || !bgHasVisiblePixel) {
                    finalColor = spriteColor;
                }

                break;
            }
        }

        framebuffer[y * SCREEN_WIDTH + x] = finalColor;
    }

    /**
     * Atualiza os shift registers durante o rendering
     */
    private void updateShifters() {
        if ((ppuMask & 0x08) != 0) {
            bgShiftPatternLow <<= 1;
            bgShiftPatternHigh <<= 1;
            bgShiftAttribLow <<= 1;
            bgShiftAttribHigh <<= 1;
        }
    }

    /**
     * Carrega os shift registers com os próximos dados de tile
     */
    private void loadBackgroundShifters() {
        bgShiftPatternLow = (bgShiftPatternLow & 0xFF00) | (bgNextTileLsb & 0xFF);
        bgShiftPatternHigh = (bgShiftPatternHigh & 0xFF00) | (bgNextTileMsb & 0xFF);

        bgShiftAttribLow = (bgShiftAttribLow & 0xFF00) | ((bgNextTileAttrib & 0x01) != 0 ? 0xFF : 0x00);
        bgShiftAttribHigh = (bgShiftAttribHigh & 0xFF00) | ((bgNextTileAttrib & 0x02) != 0 ? 0xFF : 0x00);
    }

    /**
     * Incrementa o scroll X (coarse X)
     */
    private void incrementScrollX() {
        if ((ppuMask & 0x18) == 0) return;

        if ((vramAddr & 0x001F) == 31) {
            vramAddr &= ~0x001F;
            vramAddr ^= 0x0400;
        } else {
            vramAddr++;
        }
    }

    /**
     * Incrementa o scroll Y (fine Y e coarse Y)
     */
    private void incrementScrollY() {
        if ((ppuMask & 0x18) == 0) return;

        if ((vramAddr & 0x7000) != 0x7000) {
            vramAddr += 0x1000;
        } else {
            vramAddr &= ~0x7000;
            int y = (vramAddr & 0x03E0) >> 5;
            if (y == 29) {
                y = 0;
                vramAddr ^= 0x0800;
            } else if (y == 31) {
                y = 0;
            } else {
                y++;
            }
            vramAddr = (vramAddr & ~0x03E0) | (y << 5);
        }
    }

    /**
     * Transfere bits de scroll X de t para v
     */
    private void transferAddressX() {
        if ((ppuMask & 0x18) == 0) return;

        vramAddr = (vramAddr & ~0x041F) | (tempVramAddr & 0x041F);
    }

    /**
     * Transfere bits de scroll Y de t para v
     */
    private void transferAddressY() {
        if ((ppuMask & 0x18) == 0) return;

        vramAddr = (vramAddr & ~0x7BE0) | (tempVramAddr & 0x7BE0);
    }

    /**
     * Obtém a cor do background para o pixel (x, y), usando nametable 0 (0x2000)
     * e pattern table 0 (0x0000). Ignora scroll, mas passa a usar atributos de paleta.
     */
    private int getBackgroundPixel(int x, int y) {
        if ((ppuMask & 0x08) == 0) {
            return getBackgroundColor();
        }

        if (x < 8 && (ppuMask & 0x02) == 0) {
            return getBackgroundColor();
        }

        int paletteIndexLocal = getBackgroundPaletteLocalIndex(x, y);
        if (paletteIndexLocal == 0) {
            return getBackgroundColor();
        }

        int baseCoarseX = vramAddr & 0x1F;
        int baseCoarseY = (vramAddr >> 5) & 0x1F;
        int nametableSelect = (vramAddr >> 10) & 0x03;
        int fineY = (vramAddr >> 12) & 0x07;

        int worldX = baseCoarseX * 8 + fineX + x;
        int worldY = baseCoarseY * 8 + fineY + y;

        int tileX = (worldX / 8) & 0x1F;
        int tileY = (worldY / 8) & 0x1F;

        int baseNametable = 0x2000 + (nametableSelect * 0x400);
        int attrTableBase = baseNametable + 0x3C0;
        int attrX = tileX / 4;
        int attrY = tileY / 4;
        int attrIndex = attrY * 8 + attrX;
        int attrAddr = attrTableBase + attrIndex;
        int attrByte = ppuRead(attrAddr) & 0xFF;

        int quadrantX = (tileX & 0x02) >> 1;
        int quadrantY = (tileY & 0x02) >> 1;
        int shift;
        if (quadrantY == 0 && quadrantX == 0) shift = 0;
        else if (quadrantY == 0) shift = 2;
        else if (quadrantX == 0) shift = 4;
        else shift = 6;

        int paletteHighBits = (attrByte >> shift) & 0x03;
        int paletteIndexInBytes = (paletteHighBits << 2) | paletteIndexLocal;

        int paletteEntryIndex = paletteIndexInBytes & 0x0F;
        int paletteAddr = 0x3F00 + paletteEntryIndex;
        int paletteIndex = ppuRead(paletteAddr) & 0x3F;

        return getNESColor(paletteIndex);
    }

    private int getBackgroundPaletteLocalIndex(int x, int y) {
        if ((ppuMask & 0x08) == 0) {
            return 0;
        }
        if (x < 8 && (ppuMask & 0x02) == 0) {
            return 0;
        }

        int baseCoarseX = vramAddr & 0x1F;
        int baseCoarseY = (vramAddr >> 5) & 0x1F;
        int nametableSelect = (vramAddr >> 10) & 0x03;
        int fineY = (vramAddr >> 12) & 0x07;

        int worldX = baseCoarseX * 8 + fineX + x;
        int worldY = baseCoarseY * 8 + fineY + y;

        int tileX = (worldX / 8) & 0x1F;
        int tileY = (worldY / 8) & 0x1F;
        int pixelX = worldX & 0x07;
        int pixelY = worldY & 0x07;

        int baseNametable = 0x2000 + (nametableSelect * 0x400);
        int nametableAddr = baseNametable + tileY * 32 + tileX;
        int tileIndex = ppuRead(nametableAddr) & 0xFF;

        int patternBase = (ppuCtrl & 0x10) != 0 ? 0x1000 : 0x0000;
        int patternAddr = patternBase + tileIndex * 16 + pixelY;

        int lowPlane = ppuRead(patternAddr);
        int highPlane = ppuRead(patternAddr + 8);

        int bit0 = (lowPlane >> (7 - pixelX)) & 0x01;
        int bit1 = (highPlane >> (7 - pixelX)) & 0x01;
        return (bit1 << 1) | bit0;
    }

    private int getBackgroundColor() {
        int paletteIndex = palette[0] & 0x3F;
        return getNESColor(paletteIndex);
    }

    private int getNESColor(int index) {
        final int[] nesColors = {
            0xFF545454, 0xFF001E74, 0xFF081090, 0xFF300088, 0xFF440064, 0xFF5C0030, 0xFF540400, 0xFF3C1800,
            0xFF202A00, 0xFF083A00, 0xFF004000, 0xFF003C00, 0xFF00323C, 0xFF000000, 0xFF000000, 0xFF000000,
            0xFF989698, 0xFF084CC4, 0xFF3032EC, 0xFF5C1EE4, 0xFF8814B0, 0xFFA01464, 0xFF982220, 0xFF783C00,
            0xFF545A00, 0xFF287200, 0xFF087C00, 0xFF007628, 0xFF006678, 0xFF000000, 0xFF000000, 0xFF000000,
            0xFFECEEEC, 0xFF4C9AEC, 0xFF787CEC, 0xFFB062EC, 0xFFE454EC, 0xFFEC58B4, 0xFFEC6A64, 0xFFD48820,
            0xFFA0AA00, 0xFF74C400, 0xFF4CD020, 0xFF38CC6C, 0xFF38B4CC, 0xFF3C3C3C, 0xFF000000, 0xFF000000,
            0xFFECEEEC, 0xFFA8CCEC, 0xFFBCBCEC, 0xFFD4B2EC, 0xFFECAEEC, 0xFFECAED4, 0xFFECB4B0, 0xFFE4C490,
            0xFFCCD278, 0xFFB4DE78, 0xFFA8E290, 0xFF98E2B4, 0xFFA0D6E4, 0xFFA0A2A0, 0xFF000000, 0xFF000000
        };
        if (index >= 0 && index < nesColors.length) {
            return nesColors[index];
        }
        return 0xFF000000;
    }

    public int readRegister(int register) {
        switch (register) {
            case 0x2002:
                if (sprite0Hit) {
                    ppuStatus |= 0x40;
                } else {
                    ppuStatus &= ~0x40;
                }
                int status = ppuStatus;
                ppuStatus &= ~0x80;
                nmiOccurred = false;
                writeToggle = false;
                return status;
            case 0x2004:
                return oam[oamAddr] & 0xFF;
            case 0x2007:
                int addr = vramAddr & 0x3FFF;
                int result;
                if (addr < 0x3F00) {
                    result = readBuffer & 0xFF;
                    readBuffer = ppuRead(addr);
                } else {
                    result = ppuRead(addr);
                    int mirrored = (addr - 0x1000) & 0x3FFF;
                    readBuffer = ppuRead(mirrored);
                }
                vramAddr += ((ppuCtrl & 0x04) != 0) ? 32 : 1;
                vramAddr &= 0x3FFF;
                ppuAddr = vramAddr;
                ppuData = result;
                return result & 0xFF;
            default:
                return 0;
        }
    }

    public void writeRegister(int register, int value) {
        value &= 0xFF;
        switch (register) {
            case 0x2000:
                ppuCtrl = value;
                tempVramAddr = (tempVramAddr & 0xF3FF) | ((value & 0x03) << 10);
                break;
            case 0x2001:
                ppuMask = value;
                renderingEnabled = (value & 0x18) != 0;
                break;
            case 0x2003:
                oamAddr = value;
                break;
            case 0x2004:
                oam[oamAddr] = (byte) value;
                oamAddr = (oamAddr + 1) & 0xFF;
                break;
            case 0x2005:
                if (!writeToggle) {
                    fineX = value & 0x07;
                    int coarseX = (value >> 3) & 0x1F;
                    tempVramAddr = (tempVramAddr & 0xFFE0) | coarseX;
                    writeToggle = true;
                } else {
                    int fineY = value & 0x07;
                    int coarseY = (value >> 3) & 0x1F;
                    tempVramAddr = (tempVramAddr & 0x8C1F) | (coarseY << 5) | (fineY << 12);
                    writeToggle = false;
                }
                ppuScroll = value;
                break;
            case 0x2006:
                if (!writeToggle) {
                    tempVramAddr = (tempVramAddr & 0x00FF) | ((value & 0x3F) << 8);
                    writeToggle = true;
                } else {
                    tempVramAddr = (tempVramAddr & 0x7F00) | value;
                    vramAddr = tempVramAddr & 0x3FFF;
                    writeToggle = false;
                }
                ppuAddr = vramAddr;
                break;
            case 0x2007:
                ppuData = value;
                ppuWrite(vramAddr, value);
                vramAddr += ((ppuCtrl & 0x04) != 0) ? 32 : 1;
                vramAddr &= 0x3FFF;
                ppuAddr = vramAddr;
                break;
        }
    }

    public void loadCHR(byte[] chr) {
        if (chr == null || chr.length == 0) {
            chrRom = new byte[0];
            chrRam = new byte[0x2000];
            hasChrRam = true;
        } else {
            chrRom = new byte[chr.length];
            System.arraycopy(chr, 0, chrRom, 0, chr.length);
            chrRam = null;
            hasChrRam = false;
        }
    }

    public boolean shouldTriggerNMI() {
        if (nmiOccurred) {
            nmiOccurred = false;
            return true;
        }
        return false;
    }

    public void writeOAMByte(int index, int value) {
        if (index >= 0 && index < oam.length) {
            oam[index] = (byte) (value & 0xFF);
        }
    }

    /**
     * Leitura de byte da memória PPU (VRAM, OAM, paletas, etc.)
     */
    public int ppuRead(int addr) {
        addr &= 0x3FFF;
        if (addr < 0x2000) {
            if (mapper != null) {
                return mapper.ppuRead(addr);
            }
            byte[] chrSource = hasChrRam ? chrRam : chrRom;
            return chrSource[addr & 0x1FFF] & 0xFF;
        }
        if (addr >= 0x2000 && addr < 0x3F00) {
            int mirrored = mirrorNametableAddress(addr);
            int vramIndex = (mirrored - 0x2000) & 0x07FF;
            return vram[vramIndex] & 0xFF;
        }
        if (addr < 0x4000) {
            int paletteAddr = normalizePaletteAddress(addr);
            return palette[paletteAddr - 0x3F00] & 0xFF;
        }
        return 0;
    }

    public void ppuWrite(int addr, int data) {
        addr &= 0x3FFF;
        data &= 0xFF;
        if (addr < 0x2000) {
            if (mapper != null) {
                mapper.ppuWrite(addr, data);
            } else if (hasChrRam) {
                chrRam[addr & 0x1FFF] = (byte) data;
            }
            return;
        }
        if (addr >= 0x2000 && addr < 0x3F00) {
            int mirrored = mirrorNametableAddress(addr);
            int vramIndex = (mirrored - 0x2000) & 0x07FF;
            vram[vramIndex] = (byte) data;
            return;
        }
        if (addr < 0x4000) {
            int paletteAddr = normalizePaletteAddress(addr);
            palette[paletteAddr - 0x3F00] = (byte) data;
        }
    }

    /**
     * Leitura de byte da memória PPU (VRAM, OAM, paletas, etc.) com endereçamento de 16 bits
     */
    public int ppuRead16(int addr) {
        int lo = ppuRead(addr);
        int hi = ppuRead(addr + 1);
        return (hi << 8) | lo;
    }

    public void ppuWrite16(int addr, int data) {
        ppuWrite(addr, data & 0xFF);
        ppuWrite(addr + 1, (data >> 8) & 0xFF);
    }

    public void setDebugPattern(boolean enabled) {
        debugPattern = enabled;
    }

    public boolean isDebugPattern() {
        return debugPattern;
    }

    public void setChrRom(byte[] chrRom) {
        this.chrRom = chrRom;
    }

    public void setChrRam(byte[] chrRam) {
        this.chrRam = chrRam;
        this.hasChrRam = chrRam != null;
    }

    public void setVram(byte[] vram) {
        this.vram = vram;
    }

    public void setOam(byte[] oam) {
        this.oam = oam;
    }

    public void setPalette(byte[] palette) {
        this.palette = palette;
    }

    public int[] getFramebuffer() {
        return framebuffer;
    }

    public int getPpuStatus() {
        return ppuStatus;
    }

    public boolean isNmiOccurred() {
        return nmiOccurred;
    }

    public void setRenderingEnabled(boolean enabled) {
        renderingEnabled = enabled;
    }

    public boolean isRenderingEnabled() {
        return renderingEnabled;
    }

    public int getPpuScroll() {
        return ppuScroll;
    }

    public int getPpuAddr() {
        return ppuAddr;
    }

    public int getPpuData() {
        return ppuData;
    }

    public void setPpuData(int data) {
        ppuData = data;
    }

    public void setPpuScroll(int scroll) {
        ppuScroll = scroll;
    }

    public void setPpuAddr(int addr) {
        ppuAddr = addr;
    }

    public void forceNmi() {
        nmiOccurred = true;
    }

    public void drawDebugLine(int y, int color) {
        if (y < 0 || y >= SCREEN_HEIGHT) {
            return;
        }
        for (int x = 0; x < SCREEN_WIDTH; x++) {
            framebuffer[y * SCREEN_WIDTH + x] = color;
        }
    }

    public long getFrame() {
        return frame;
    }

    public void setMirrorMode(MirrorMode mode) {
        this.mirrorMode = mode;
    }

    private int mirrorNametableAddress(int addr) {
        int offset = (addr - 0x2000) & 0x0FFF;
        switch (mirrorMode) {
            case VERTICAL:
                return 0x2000 + (offset % 0x800);
            case HORIZONTAL:
                return 0x2000 + ((offset & 0x3FF) | ((offset & 0x800) != 0 ? 0x400 : 0));
            case FOUR_SCREEN:
                return 0x2000 + offset;
            case SINGLE_SCREEN_LOW:
                return 0x2000 + (offset & 0x3FF);
            case SINGLE_SCREEN_HIGH:
                return 0x2400 + (offset & 0x3FF);
            default:
                return 0x2000 + (offset % 0x800);
        }
    }

    public void setMapper(Mapper mapper) {
        this.mapper = mapper;
    }

    private int normalizePaletteAddress(int addr) {
        int paletteAddr = 0x3F00 + ((addr - 0x3F00) & 0x1F);
        if ((paletteAddr & 0x03) == 0) {
            paletteAddr &= ~0x10;
        }
        return paletteAddr;
    }
}
